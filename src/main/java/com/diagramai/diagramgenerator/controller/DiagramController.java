package com.diagramai.diagramgenerator.controller;

import com.diagramai.diagramgenerator.modal.DiagramRequest;
import com.diagramai.diagramgenerator.modal.DiagramResponse;
import com.diagramai.diagramgenerator.modal.ImageDiagramRequest;
import com.diagramai.diagramgenerator.service.DiagramService;
import com.diagramai.diagramgenerator.service.ImageDiagramService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DiagramController {

    private final DiagramService diagramService;
    private final ImageDiagramService imageDiagramService;

    @PostMapping("/generate-diagram")
    public ResponseEntity<DiagramResponse> generateDiagram(@RequestBody DiagramRequest request) {
        DiagramResponse response = diagramService.generateAllDiagrams(request.getProjectText());
        if (response.getError() != null && !response.getError().isEmpty()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-from-image")
    public ResponseEntity<DiagramResponse> generateFromImage(@RequestBody ImageDiagramRequest request) {
        DiagramResponse response = imageDiagramService.generateFromImage(
                request.getImageBase64(),
                request.getMimeType()
        );
        if (response.getError() != null && !response.getError().isEmpty()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }


//    @PostMapping("/generate-from-image")
//    public ResponseEntity<DiagramResponse> generateFromImage(
//            @RequestParam("image") MultipartFile image) {
//
//        DiagramResponse response =
//                imageDiagramService.generateFromImage(image);
//
//        return ResponseEntity.ok(response);
//    }
}