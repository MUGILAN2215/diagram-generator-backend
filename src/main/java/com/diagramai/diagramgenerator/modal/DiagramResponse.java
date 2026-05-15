package com.diagramai.diagramgenerator.modal;

import lombok.Data;

@Data
public class DiagramResponse {
    private WorkflowDiagram workflow;
    private FunctionalBlockDiagram functionalBlock;
    private SystemArchitecture systemArchitecture;
    private String error;
}