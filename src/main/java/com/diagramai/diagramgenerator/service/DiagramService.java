package com.diagramai.diagramgenerator.service;

import com.diagramai.diagramgenerator.modal.*;
import com.diagramai.diagramgenerator.validator.DiagramValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DiagramService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final DiagramValidator validator;

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    public DiagramResponse generateAllDiagrams(String projectText) {
        DiagramResponse response = new DiagramResponse();

        if (projectText == null || projectText.trim().length() < 20) {
            response.setError("Description too short. Please provide more detail.");
            return response;
        }

        String cleanedText = projectText.trim().replaceAll("\\s+", " ");

        try {
            // Generate all 3 diagrams
            WorkflowDiagram workflow = generateWorkflow(cleanedText);
            FunctionalBlockDiagram functionalBlock = generateFunctionalBlock(cleanedText);
            SystemArchitecture systemArchitecture = generateSystemArchitecture(cleanedText);

            response.setWorkflow(workflow);
            response.setFunctionalBlock(functionalBlock);
            response.setSystemArchitecture(systemArchitecture);

            // Validate all diagrams
            if (!validator.validate(response)) {
                response.setError("AI returned invalid diagram structure. Please try again.");
                return response;
            }

            return response;

        } catch (Exception e) {
            response.setError("Failed to generate diagrams: " + e.getMessage());
            return response;
        }
    }

    private WorkflowDiagram generateWorkflow(String projectText) throws Exception {
        String prompt = String.format(DiagramPrompts.WORKFLOW_PROMPT, projectText);
        String jsonResponse = callGemini(prompt);
        return objectMapper.readValue(jsonResponse, WorkflowDiagram.class);
    }

    private FunctionalBlockDiagram generateFunctionalBlock(String projectText) throws Exception {
        String prompt = String.format(DiagramPrompts.FUNCTIONAL_BLOCK_PROMPT, projectText);
        String jsonResponse = callGemini(prompt);
        return objectMapper.readValue(jsonResponse, FunctionalBlockDiagram.class);
    }

    private SystemArchitecture generateSystemArchitecture(String projectText) throws Exception {
        String prompt = String.format(DiagramPrompts.SYSTEM_ARCHITECTURE_PROMPT, projectText);
        String jsonResponse = callGemini(prompt);
        return objectMapper.readValue(jsonResponse, SystemArchitecture.class);
    }

    private String callGemini(String prompt) throws Exception {
        // Build Gemini request body
        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> requestBody = Map.of("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // Call Gemini API
        ResponseEntity<Map> response = restTemplate.postForEntity(
                GEMINI_URL + apiKey, request, Map.class
        );

        // Extract text from Gemini response
        List<Map> candidates = (List<Map>) response.getBody().get("candidates");
        Map firstCandidate = candidates.get(0);
        Map contentMap = (Map) firstCandidate.get("content");
        List<Map> parts = (List<Map>) contentMap.get("parts");
        String aiText = (String) parts.get(0).get("text");

        // Clean JSON (remove markdown if present)
        String cleanedJson = aiText
                .replace("```json", "")
                .replace("```", "")
                .trim();

        return cleanedJson;
    }
}