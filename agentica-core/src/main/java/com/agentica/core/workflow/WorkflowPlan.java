package com.agentica.core.workflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Structured output from the Orchestrator LLM.
 * Defines a complete workflow that will be built into a LangGraph4j StateGraph.
 */
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record WorkflowPlan(

    /**
     * Unique identifier for this workflow.
     */
    @NotBlank
    String workflowId,

    /**
     * Human-readable name for the workflow.
     */
    @NotBlank
    String workflowName,

    /**
     * Description of what this workflow accomplishes.
     */
    @NotBlank
    String description,

    /**
     * The nodes (agent steps) in the workflow.
     */
    @NotNull
    @Valid
    List<WorkflowNode> nodes,

    /**
     * The edges (transitions) between nodes.
     */
    @NotNull
    @Valid
    List<WorkflowEdge> edges,

    /**
     * The ID of the entry point node (first node to execute).
     */
    @NotBlank
    String entryPoint,

    /**
     * Initial state values for the workflow execution.
     */
    Map<String, Object> initialState,

    /**
     * Version number for tracking workflow modifications during replanning.
     */
    int version

) implements Serializable {

    /**
     * Creates a new workflow plan with an updated version number.
     *
     * @param plan the original plan
     * @param newVersion the new version number
     * @return a new WorkflowPlan with the updated version
     */
    public static WorkflowPlan withVersion(WorkflowPlan plan, int newVersion) {

        return plan.toBuilder()
            .version(newVersion)
            .build();
    }

}
