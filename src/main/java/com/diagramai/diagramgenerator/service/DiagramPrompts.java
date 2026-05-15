package com.diagramai.diagramgenerator.service;

public class DiagramPrompts {

    public static final String WORKFLOW_PROMPT = """
        You are an expert system architect analyzing a project to create a WORKFLOW DIAGRAM.
        
        Your task: Show HOW the process flows from start to finish.
        
        CORE PRINCIPLES (not rigid rules):
        - Workflows show sequential processes, decisions, and task flow
        - Must have clear start and end points
        - Decision points should show branching logic (conditions matter)
        - Can include parallel processes if the system does things simultaneously
        - Can show loops/retries if the system repeats steps
        - Focus on the PROCESS, not technical implementation
        
        NODE TYPES - choose what fits best:
        - "start/end" for beginning/completion
        - "process" for actions/tasks
        - "decision" for conditional branching
        - "input/output" for data entry/display
        - "parallel" if multiple things happen at once
        - "wait" for delays/timers
        - Invent new types if needed for clarity
        
        IMPORTANT:
        - Don't force a template — understand the actual system flow
        - If it's event-driven, show events
        - If it's async, show parallel paths
        - If it has retries, show loops
        - Be creative but accurate
        
        Return ONLY valid JSON:
        {
          "nodes": [
            {"id": "unique_id", "label": "descriptive name", "type": "appropriate_type"}
          ],
          "edges": [
            {"source": "id1", "target": "id2", "label": "condition if decision"}
          ]
        }
        
        Project Description:
        %s
        """;

    public static final String FUNCTIONAL_BLOCK_PROMPT = """
        You are an expert system architect creating a FUNCTIONAL BLOCK DIAGRAM.
        
        Your task: Show WHAT major functions/modules exist and how they interact.
        
        CORE PRINCIPLES:
        - Focus on functional capabilities, not code structure
        - Each block represents a major function/responsibility
        - Show data/control flow between functions
        - Group related functions logically
        - External systems should be clearly marked
        - Use verb-based names that describe what each function DOES
        
        FLEXIBILITY:
        - If it's a microservices system, show services as functions
        - If it's an AI pipeline, show processing stages
        - If it's IoT, show sensor processing and control functions
        - If it's event-driven, show event handlers as functions
        - Don't force layered structure if the system isn't layered
        
        NODE ATTRIBUTES - adapt to the system:
        - "type": describe the function's role (input, processing, output, storage, external, AI, cache, queue, orchestrator, etc.)
        - "layer": group related functions (optional, only if system has clear layers)
        - Use natural groupings, not forced categories
        
        IMPORTANT:
        - Understand the ACTUAL system architecture
        - Don't assume web app patterns for non-web systems
        - Don't invent components not mentioned
        - Be accurate to what's described
        
        Return ONLY valid JSON:
        {
          "nodes": [
            {"id": "unique_id", "label": "Function Name", "type": "function_role", "layer": "optional_grouping"}
          ],
          "edges": [
            {"source": "id1", "target": "id2"}
          ]
        }
        
        Project Description:
        %s
        """;

    public static final String SYSTEM_ARCHITECTURE_PROMPT = """
        You are an expert software architect creating a SYSTEM ARCHITECTURE DIAGRAM.
        
        Your task: Show HOW the entire system is structurally organized — components, technologies, and their relationships.
        
        CORE PRINCIPLES:
        - Show the actual architecture, not a template
        - Identify real components from the description
        - Show communication patterns (sync, async, events, APIs)
        - Label technologies accurately as mentioned
        - Distinguish internal vs external systems
        - Show data stores separately
        
        ARCHITECTURE PATTERNS - recognize and adapt:
        - Monolith: single backend, integrated database
        - Layered: presentation → business → data layers
        - Microservices: independent services communicating via APIs/events
        - Event-driven: event bus, producers, consumers
        - Serverless: functions, managed services
        - AI/ML Pipeline: data ingestion → processing → model → inference
        - IoT: devices → gateway → processing → storage
        - Don't force a pattern — identify what's actually described
        
        NODE ATTRIBUTES - be specific and accurate:
        - "type": frontend, backend, database, cache, queue, service, api, external, ai-model, worker, load-balancer, etc.
        - "layer": logical grouping if clear layers exist (presentation, application, business, data, infrastructure, external)
        - "tech": actual technology mentioned (React, Spring Boot, PostgreSQL, Kafka, Redis, Lambda, etc.)
        
        EDGE ATTRIBUTES - show communication clearly:
        - "protocol": REST, GraphQL, WebSocket, MQTT, JDBC, gRPC, Events, Message Queue, etc.
        - "mode": sync or async (if relevant)
        
        CRITICAL RULES:
        - Only include components explicitly mentioned or clearly implied
        - Don't add Redis/Kafka/Docker if not mentioned
        - Don't assume React if "web app" is mentioned — use generic "Web Frontend"
        - Don't invent technologies
        - Be accurate, not creative with tech choices
        
        Return ONLY valid JSON:
        {
          "nodes": [
            {"id": "unique_id", "label": "Component Name", "type": "component_type", "layer": "optional_layer", "tech": "actual_technology"}
          ],
          "edges": [
            {"source": "id1", "target": "id2", "protocol": "communication_method"}
          ]
        }
        
        Project Description:
        %s
        """;
}