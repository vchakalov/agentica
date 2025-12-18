package com.agentica.workflows.debug;

import com.agentica.core.workflow.ReplanAction;
import com.agentica.core.workflow.ReplanDecision;
import com.agentica.core.workflow.WorkflowPlan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;

import java.time.Instant;

/**
 * Captures complete details of a workflow replan event.
 * Used for debugging and visualization.
 */
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReplanInfo(

    int replanNumber,

    String triggerNodeId,

    String escalationReason,

    String escalationContext,

    ReplanAction action,

    String guidance,

    String abortReason,

    String resumeFrom,

    WorkflowPlan previousPlan,

    WorkflowPlan newPlan,

    /**
     * The prompt sent to the orchestrator LLM for replanning.
     */
    String llmPrompt,

    /**
     * The raw response from the orchestrator LLM.
     */
    String llmResponse,

    Instant timestamp

) {

    /**
     * Creates a ReplanInfo from a WorkflowReplanEvent.
     */
    public static ReplanInfo from(
            int replanNumber,
            String triggerNodeId,
            String escalationReason,
            String escalationContext,
            ReplanDecision decision,
            WorkflowPlan previousPlan,
            WorkflowPlan newPlan,
            String llmPrompt,
            String llmResponse) {

        return ReplanInfo.builder()
            .replanNumber(replanNumber)
            .triggerNodeId(triggerNodeId)
            .escalationReason(escalationReason)
            .escalationContext(escalationContext)
            .action(decision != null ? decision.action() : null)
            .guidance(decision != null ? decision.guidance() : null)
            .abortReason(decision != null ? decision.reason() : null)
            .resumeFrom(decision != null ? decision.resumeFrom() : null)
            .previousPlan(previousPlan)
            .newPlan(newPlan)
            .llmPrompt(llmPrompt)
            .llmResponse(llmResponse)
            .timestamp(Instant.now())
            .build();
    }

}
