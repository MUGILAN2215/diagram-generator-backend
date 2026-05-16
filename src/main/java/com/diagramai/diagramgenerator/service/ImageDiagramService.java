package com.diagramai.diagramgenerator.service;

import com.diagramai.diagramgenerator.modal.*;
import com.diagramai.diagramgenerator.validator.DiagramValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ImageDiagramService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final DiagramValidator validator;
    private final PositionService positionService;

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_VISION_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    private static final String VISION_WORKFLOW_PROMPT = """
        You are an expert diagram analyst. Look at this image carefully.
        
        Extract a WORKFLOW DIAGRAM from it:
        - Identify every node/box/shape and its label
        - Identify every arrow/connection between them
        - Detect edge style: use "dotted" for dashed/dotted arrows, "solid" for normal arrows
        - For decision nodes (diamond shapes), set type to "decision"
        - For start/end (rounded/oval), set type to "start" or "end"
        - For regular steps (rectangles), set type to "process"
        
        Return ONLY valid JSON:
        {
          "nodes": [
            {"id": "n1", "label": "Step Name", "type": "process"}
          ],
          "edges": [
            {"source": "n1", "target": "n2", "label": "condition or empty", "style": "solid"}
          ]
        }
        """;

    private static final String VISION_FUNCTIONAL_PROMPT = """
        You are an expert diagram analyst. Look at this image carefully.
        
        Extract a FUNCTIONAL BLOCK DIAGRAM from it:
        - Identify every block/module and its label
        - Identify every connection between blocks
        - Detect edge style: use "dotted" for dashed lines, "solid" for solid lines
        - Identify the layer/group each block belongs to (e.g., "frontend", "backend", "database")
        
        Return ONLY valid JSON:
        {
          "nodes": [
            {"id": "n1", "label": "Module Name", "type": "processing", "layer": "backend"}
          ],
          "edges": [
            {"source": "n1", "target": "n2", "style": "solid"}
          ]
        }
        """;

    private static final String VISION_ARCHITECTURE_PROMPT = """
        You are an expert diagram analyst. Look at this image carefully.
        
        Extract a SYSTEM ARCHITECTURE DIAGRAM from it:
        - Identify every component and its label
        - Identify all connections and their protocols if labeled
        - Detect edge style: use "dotted" for dashed lines, "solid" for solid lines
        - Detect the layer each component belongs to (presentation, backend, data, external, etc.)
        - Detect technology labels if written on or near the component
        
        Return ONLY valid JSON:
        {
          "nodes": [
            {"id": "n1", "label": "Component", "type": "backend", "layer": "application", "tech": "Spring Boot"}
          ],
          "edges": [
            {"source": "n1", "target": "n2", "protocol": "REST", "style": "solid"}
          ]
        }
        """;

    public DiagramResponse generateFromImage(String imageBase64, String mimeType) {
        DiagramResponse response = new DiagramResponse();

        if (imageBase64 == null || imageBase64.isBlank()) {
            response.setError("No image provided.");
            return response;
        }

        try {
            WorkflowDiagram workflow = extractWorkflow(imageBase64, mimeType);
            FunctionalBlockDiagram functional = extractFunctionalBlock(imageBase64, mimeType);
            SystemArchitecture architecture = extractSystemArchitecture(imageBase64, mimeType);

            positionService.assignWorkflowPositions(workflow.getNodes(), workflow.getEdges());
            positionService.assignLayeredPositions(functional.getNodes(), functional.getEdges());
            positionService.assignLayeredPositions(architecture.getNodes(), architecture.getEdges());

            response.setWorkflow(workflow);
            response.setFunctionalBlock(functional);
            response.setSystemArchitecture(architecture);

            if (!validator.validate(response)) {
                response.setError("Could not extract valid diagram structure from the image.");
                return response;
            }

            return response;

        } catch (Exception e) {
            response.setError("Failed to process image: " + e.getMessage());
            return response;
        }
    }

    private WorkflowDiagram extractWorkflow(String imageBase64, String mimeType) throws Exception {
        String json = callGeminiVision(VISION_WORKFLOW_PROMPT, imageBase64, mimeType);
        return objectMapper.readValue(json, WorkflowDiagram.class);
    }

    private FunctionalBlockDiagram extractFunctionalBlock(String imageBase64, String mimeType) throws Exception {
        String json = callGeminiVision(VISION_FUNCTIONAL_PROMPT, imageBase64, mimeType);
        return objectMapper.readValue(json, FunctionalBlockDiagram.class);
    }

    private SystemArchitecture extractSystemArchitecture(String imageBase64, String mimeType) throws Exception {
        String json = callGeminiVision(VISION_ARCHITECTURE_PROMPT, imageBase64, mimeType);
        return objectMapper.readValue(json, SystemArchitecture.class);
    }

    private String callGeminiVision(String prompt, String imageBase64, String mimeType) throws Exception {
        // Build multimodal request: text prompt + inline image
        Map<String, Object> textPart = Map.of("text", prompt);

        Map<String, String> imageData = new HashMap<>();
        imageData.put("mime_type", mimeType);
        imageData.put("data", imageBase64);

        Map<String, Object> imagePart = Map.of("inline_data", imageData);

        Map<String, Object> content = Map.of("parts", List.of(textPart, imagePart));
        Map<String, Object> requestBody = Map.of("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                GEMINI_VISION_URL + apiKey, request, Map.class
        );

        List<Map> candidates = (List<Map>) response.getBody().get("candidates");
        Map firstCandidate = candidates.get(0);
        Map contentMap = (Map) firstCandidate.get("content");
        List<Map> parts = (List<Map>) contentMap.get("parts");
        String aiText = (String) parts.get(0).get("text");

        return aiText
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }
}