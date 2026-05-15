package com.diagramai.diagramgenerator.controller;

import com.diagramai.diagramgenerator.modal.DiagramRequest;
import com.diagramai.diagramgenerator.modal.DiagramResponse;
import com.diagramai.diagramgenerator.service.DiagramService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DiagramController {

    private final DiagramService diagramService;

    @PostMapping("/generate-diagram")
    public ResponseEntity<DiagramResponse> generateDiagram(@RequestBody DiagramRequest request) {

        DiagramResponse response = diagramService.generateAllDiagrams(request.getProjectText());

        if (response.getError() != null && !response.getError().isEmpty()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }
}