package com.agentica.core.workflow;

import com.agentica.core.domain.Event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Builder;

import java.util.Map;

/**
 * Request sent to the Orchestrator LLM for replanning a workflow.
 * Contains context about the current workflow state and the escalation.
 */
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReplanRequest(

    /**
     * The original event that triggered the workflow.
     */
    @NotNull
    Event event,

    /**
     * The current workflow plan that needs modification.
     */
    @NotNull
    WorkflowPlan currentPlan,

    /**
     * The node that triggered the escalation.
     */
    @NotBlank
    String escalatingNodeId,

    /**
     * The reason for escalation.
     */
    @NotBlank
    String escalationReason,

    /**
     * Additional context about the escalation (e.g., error message, state snapshot).
     */
    String escalationContext,

    /**
     * The current state of the workflow execution.
     */
    Map<String, Object> currentState,

    /**
     * Outputs from nodes that have already executed.
     */
    Map<String, Object> nodeOutputs

) {}
