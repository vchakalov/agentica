package com.agentica.core.workflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotNull;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

/**
 * Structured output from the Orchestrator LLM during replanning.
 * Defines how to modify the workflow based on agent escalation.
 */
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReplanDecision(

    /**
     * The type of replan action to take.
     */
    @NotNull
    ReplanAction action,

    /**
     * The complete modified workflow plan.
     * Used when action is MODIFY_PLAN.
     */
    WorkflowPlan modifiedPlan,

    /**
     * New nodes to add to the existing plan.
     * Used when action is ADD_NODES.
     */
    List<WorkflowNode> newNodes,

    /**
     * New edges to add to the existing plan.
     * Used when action is ADD_NODES.
     */
    List<WorkflowEdge> newEdges,

    /**
     * Additional guidance for retrying the current step.
     * Used when action is RETRY_WITH_GUIDANCE.
     */
    String guidance,

    /**
     * Reason for aborting the workflow.
     * Used when action is ABORT.
     */
    String reason,

    /**
     * The node ID to resume execution from after replanning.
     */
    String resumeFrom

) implements Serializable {}
