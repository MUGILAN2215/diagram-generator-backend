package com.diagramai.diagramgenerator.modal;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class FunctionalBlockDiagram {
    private String diagramType = "functionalBlock";
    private List<Map<String, String>> nodes;
    private List<Map<String, String>> edges;
}