package com.diagramai.diagramgenerator.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PositionService {

    private static final int H_GAP = 220;  // horizontal spacing
    private static final int V_GAP = 120;  // vertical spacing
    private static final int START_X = 100;
    private static final int START_Y = 80;

    /**
     * Assigns x, y to each node for a top-to-bottom flow (Workflow).
     * Builds a simple topological level from edges and spreads nodes per level.
     */
    public void assignWorkflowPositions(List<Map<String, String>> nodes,
                                        List<Map<String, String>> edges) {
        Map<String, Integer> levelMap = buildLevelMap(nodes, edges);

        // Group nodes by level
        Map<Integer, List<String>> levelGroups = new TreeMap<>();
        for (Map.Entry<String, Integer> entry : levelMap.entrySet()) {
            levelGroups.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }

        // Assign positions
        Map<String, int[]> positions = new HashMap<>();
        for (Map.Entry<Integer, List<String>> entry : levelGroups.entrySet()) {
            int level = entry.getKey();
            List<String> ids = entry.getValue();
            int totalWidth = (ids.size() - 1) * H_GAP;
            int startX = START_X + Math.max(0, (600 - totalWidth) / 2);

            for (int i = 0; i < ids.size(); i++) {
                positions.put(ids.get(i), new int[]{startX + i * H_GAP, START_Y + level * V_GAP});
            }
        }

        applyPositions(nodes, positions);
    }

    /**
     * Assigns x, y to each node grouped by "layer" field (Functional Block / System Architecture).
     * Nodes with same layer go on the same row.
     */
    public void assignLayeredPositions(List<Map<String, String>> nodes,
                                       List<Map<String, String>> edges) {
        // Group by layer (fall back to "default" if missing)
        Map<String, List<Map<String, String>>> layerGroups = new LinkedHashMap<>();
        for (Map<String, String> node : nodes) {
            String layer = node.getOrDefault("layer", "default");
            layerGroups.computeIfAbsent(layer, k -> new ArrayList<>()).add(node);
        }

        Map<String, int[]> positions = new HashMap<>();
        int row = 0;
        for (List<Map<String, String>> group : layerGroups.values()) {
            for (int col = 0; col < group.size(); col++) {
                String id = group.get(col).get("id");
                positions.put(id, new int[]{START_X + col * H_GAP, START_Y + row * V_GAP});
            }
            row++;
        }

        applyPositions(nodes, positions);
    }

    // ---------- helpers ----------

    private Map<String, Integer> buildLevelMap(List<Map<String, String>> nodes,
                                               List<Map<String, String>> edges) {
        // In-degree map
        Map<String, Set<String>> parents = new HashMap<>();
        for (Map<String, String> node : nodes) {
            parents.put(node.get("id"), new HashSet<>());
        }
        for (Map<String, String> edge : edges) {
            String target = edge.get("target");
            String source = edge.get("source");
            if (target != null && source != null && parents.containsKey(target)) {
                parents.get(target).add(source);
            }
        }

        // BFS level assignment
        Map<String, Integer> level = new HashMap<>();
        Queue<String> queue = new LinkedList<>();

        for (Map<String, String> node : nodes) {
            String id = node.get("id");
            if (parents.get(id).isEmpty()) {
                level.put(id, 0);
                queue.add(id);
            }
        }

        // Build children map for BFS
        Map<String, List<String>> children = new HashMap<>();
        for (Map<String, String> node : nodes) children.put(node.get("id"), new ArrayList<>());
        for (Map<String, String> edge : edges) {
            String src = edge.get("source");
            String tgt = edge.get("target");
            if (src != null && tgt != null && children.containsKey(src)) {
                children.get(src).add(tgt);
            }
        }

        while (!queue.isEmpty()) {
            String current = queue.poll();
            int currentLevel = level.getOrDefault(current, 0);
            for (String child : children.getOrDefault(current, List.of())) {
                int newLevel = currentLevel + 1;
                if (!level.containsKey(child) || level.get(child) < newLevel) {
                    level.put(child, newLevel);
                    queue.add(child);
                }
            }
        }

        // Any unvisited nodes (cycles) get assigned last level
        int maxLevel = level.values().stream().mapToInt(i -> i).max().orElse(0);
        for (Map<String, String> node : nodes) {
            level.putIfAbsent(node.get("id"), maxLevel + 1);
        }

        return level;
    }

    private void applyPositions(List<Map<String, String>> nodes, Map<String, int[]> positions) {
        for (Map<String, String> node : nodes) {
            String id = node.get("id");
            int[] pos = positions.getOrDefault(id, new int[]{START_X, START_Y});
            // nodes is Map<String,String> so we store as string
            node.put("x", String.valueOf(pos[0]));
            node.put("y", String.valueOf(pos[1]));
        }
    }
}