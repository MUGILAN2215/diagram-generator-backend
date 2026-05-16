package com.diagramai.diagramgenerator.modal;

import lombok.Data;

@Data
public class ImageDiagramRequest {
    // Base64-encoded image string sent by frontend
    private String imageBase64;
    // e.g. "image/png", "image/jpeg"
    private String mimeType;
}