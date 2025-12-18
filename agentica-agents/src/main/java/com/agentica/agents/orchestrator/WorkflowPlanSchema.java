package com.agentica.agents.orchestrator;

import com.agentica.core.enums.AgentType;
import com.agentica.core.workflow.EdgeType;
import com.agentica.core.workflow.ReplanAction;

import com.google.genai.types.Schema;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Builds Google GenAI Schema definitions for structured output from the Orchestrator LLM.
 * These schemas ensure the LLM outputs valid JSON matching our WorkflowPlan structure.
 */
@Component
public class WorkflowPlanSchema {

    /**
     * Builds the schema for WorkflowPlan structured output.
     *
     * @return Schema for WorkflowPlan
     */
    public Schema buildWorkflowPlanSchema() {

        return Schema.builder()
            .type("OBJECT")
            .properties(Map.of(
                "workflowId", Schema.builder()
                    .type("STRING")
                    .description("Unique identifier for this workflow")
                    .build(),
                "workflowName", Schema.builder()
                    .type("STRING")
                    .description("Human-readable name for the workflow")
                    .build(),
                "description", Schema.builder()
                    .type("STRING")
                    .description("Description of what this workflow accomplishes")
                    .build(),
                "entryPoint", Schema.builder()
                    .type("STRING")
                    .description("ID of the first node to execute")
                    .build(),
                "nodes", buildNodesSchema(),
                "edges", buildEdgesSchema()
            ))
            .required(List.of("workflowId", "workflowName", "description", "entryPoint", "nodes", "edges"))
            .build();
    }

    /**
     * Builds the schema for ReplanDecision structured output.
     *
     * @return Schema for ReplanDecision
     */
    public Schema buildReplanDecisionSchema() {

        List<String> replanActions = Arrays.stream(ReplanAction.values())
            .map(Enum::name)
            .toList();

        return Schema.builder()
            .type("OBJECT")
            .properties(Map.of(
                "action", Schema.builder()
                    .type("STRING")
                    .enum_(replanActions)
                    .description("The type of replan action to take")
                    .build(),
                "modifiedPlan", buildWorkflowPlanSchema(),
                "newNodes", buildNodesSchema(),
                "newEdges", buildEdgesSchema(),
                "guidance", Schema.builder()
                    .type("STRING")
                    .description("Additional guidance for retrying the current step")
                    .build(),
                "reason", Schema.builder()
                    .type("STRING")
                    .description("Reason for aborting the workflow")
                    .build(),
                "resumeFrom", Schema.builder()
                    .type("STRING")
                    .description("Node ID to resume execution from")
                    .build()
            ))
            .required(List.of("action"))
            .build();
    }

    private Schema buildNodesSchema() {

        // Only include agents that are actually implemented
        List<String> agentTypes = List.of("MARKETING", "FACEBOOK", "REVIEW");

        return Schema.builder()
            .type("ARRAY")
            .items(Schema.builder()
                .type("OBJECT")
                .properties(Map.of(
                    "id", Schema.builder()
                        .type("STRING")
                        .description("Unique identifier for this node within the workflow")
                        .build(),
                    "agentType", Schema.builder()
                        .type("STRING")
                        .enum_(agentTypes)
                        .description("Type of agent to execute at this node")
                        .build(),
                    "description", Schema.builder()
                        .type("STRING")
                        .description("Human-readable description of what this node does")
                        .build(),
                    "instruction", Schema.builder()
                        .type("STRING")
                        .description("Optional instruction override for the agent")
                        .build()
                ))
                .required(List.of("id", "agentType", "description"))
                .build())
            .build();
    }

    private Schema buildEdgesSchema() {

        List<String> edgeTypes = Arrays.stream(EdgeType.values())
            .map(Enum::name)
            .toList();

        return Schema.builder()
            .type("ARRAY")
            .items(Schema.builder()
                .type("OBJECT")
                .properties(Map.of(
                    "from", Schema.builder()
                        .type("STRING")
                        .description("Source node ID (use 'START' for entry point)")
                        .build(),
                    "to", Schema.builder()
                        .type("STRING")
                        .description("Target node ID (use 'END' for workflow completion)")
                        .build(),
                    "type", Schema.builder()
                        .type("STRING")
                        .enum_(edgeTypes)
                        .description("Type of edge (DIRECT or CONDITIONAL)")
                        .build(),
                    "condition", buildConditionSchema()
                ))
                .required(List.of("from", "to", "type"))
                .build())
            .build();
    }

    private Schema buildConditionSchema() {

        return Schema.builder()
            .type("OBJECT")
            .description("Condition for routing. MUST include both stateKey and routes array.")
            .properties(Map.of(
                "stateKey", Schema.builder()
                    .type("STRING")
                    .description("State key to evaluate. Use 'routing_decision' for MARKETING agent output.")
                    .build(),
                "routes", Schema.builder()
                    .type("ARRAY")
                    .description("REQUIRED: Array of {value, target} objects mapping routing_decision values to node IDs")
                    .items(Schema.builder()
                        .type("OBJECT")
                        .properties(Map.of(
                            "value", Schema.builder()
                                .type("STRING")
                                .description("The routing_decision value to match (e.g., 'respond', 'delete')")
                                .build(),
                            "target", Schema.builder()
                                .type("STRING")
                                .description("The target node ID to route to when value matches")
                                .build()
                        ))
                        .required(List.of("value", "target"))
                        .build())
                    .build()
            ))
            .required(List.of("stateKey", "routes"))
            .build();
    }

}
