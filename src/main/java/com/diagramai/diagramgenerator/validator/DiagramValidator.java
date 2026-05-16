package com.diagramai.diagramgenerator.validator;

import com.diagramai.diagramgenerator.modal.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DiagramValidator {

    public boolean validate(DiagramResponse response) {
        if (response == null) {
            return false;
        }

        // If there's an error, skip validation
        if (response.getError() != null && !response.getError().isEmpty()) {
            return false;
        }

        // Validate all 3 diagrams
        boolean workflowValid = validateWorkflow(response.getWorkflow());
        boolean functionalValid = validateFunctionalBlock(response.getFunctionalBlock());
        boolean architectureValid = validateSystemArchitecture(response.getSystemArchitecture());

        return workflowValid && functionalValid && architectureValid;
    }

    private boolean validateWorkflow(WorkflowDiagram workflow) {
        if (workflow == null) {
            return false;
        }
        return validateDiagramStructure(workflow.getNodes(), workflow.getEdges());
    }

    private boolean validateFunctionalBlock(FunctionalBlockDiagram functionalBlock) {
        if (functionalBlock == null) {
            return false;
        }
        return validateDiagramStructure(functionalBlock.getNodes(), functionalBlock.getEdges());
    }

    private boolean validateSystemArchitecture(SystemArchitecture architecture) {
        if (architecture == null) {
            return false;
        }
        return validateDiagramStructure(architecture.getNodes(), architecture.getEdges());
    }

    private boolean validateDiagramStructure(List<Map<String, String>> nodes,
                                             List<Map<String, String>> edges) {
        // Check nodes exist
        if (nodes == null || nodes.isEmpty()) {
            return false;
        }

        // Check edges exist
        if (edges == null || edges.isEmpty()) {
            return false;
        }

        // Collect all node IDs
        Set<String> nodeIds = new HashSet<>();
        for (Map<String, String> node : nodes) {
            String id = node.get("id");
            if (id == null || id.trim().isEmpty()) {
                return false; // Invalid node ID
            }

            String label = node.get("label");
            if (label == null || label.trim().isEmpty()) {
                return false; // Invalid node label
            }

            nodeIds.add(id);
        }

        // Check for duplicate node IDs
        if (nodeIds.size() != nodes.size()) {
            return false; // Duplicate IDs found
        }

//        // Remove duplicate node IDs instead of rejecting
//        nodes.removeIf(node -> !nodeIds.add(node.get("id")));

        // Validate edges
        for (Map<String, String> edge : edges) {
            String source = edge.get("source");
            String target = edge.get("target");

            if (source == null || target == null) {
                return false; // Missing source or target
            }

            if (!nodeIds.contains(source)) {
                return false; // Source node doesn't exist
            }

            if (!nodeIds.contains(target)) {
                return false; // Target node doesn't exist
            }
        }

        return true;
    }
}