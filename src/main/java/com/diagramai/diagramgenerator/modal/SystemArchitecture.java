package com.diagramai.diagramgenerator.modal;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SystemArchitecture {
    private String diagramType = "systemArchitecture";
    private List<Map<String, String>> nodes;
    private List<Map<String, String>> edges;
}