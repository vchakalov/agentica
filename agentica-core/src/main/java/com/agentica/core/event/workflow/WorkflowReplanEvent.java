package com.agentica.core.event.workflow;

import com.agentica.core.workflow.ReplanDecision;
import com.agentica.core.workflow.WorkflowPlan;

import lombok.Builder;

import java.time.Instant;

/**
 * Published when the orchestrator replans a workflow mid-execution.
 */
@Builder
public record WorkflowReplanEvent(

    String eventId,

    WorkflowPlan previousPlan,

    WorkflowPlan newPlan,

    ReplanDecision decision,

    String triggerNodeId,

    String escalationReason,

    String escalationContext,

    int replanNumber,

    /**
     * The prompt sent to the orchestrator LLM for replanning.
     */
    String llmPrompt,

    /**
     * The raw response from the orchestrator LLM.
     */
    String llmResponse,

    Instant timestamp

) implements WorkflowEvent {

    public static WorkflowReplanEvent of(
            String eventId,
            WorkflowPlan previousPlan,
            WorkflowPlan newPlan,
            ReplanDecision decision,
            String triggerNodeId,
            String escalationReason,
            String escalationContext,
            int replanNumber,
            String llmPrompt,
            String llmResponse) {

        return WorkflowReplanEvent.builder()
            .eventId(eventId)
            .previousPlan(previousPlan)
            .newPlan(newPlan)
            .decision(decision)
            .triggerNodeId(triggerNodeId)
            .escalationReason(escalationReason)
            .escalationContext(escalationContext)
            .replanNumber(replanNumber)
            .llmPrompt(llmPrompt)
            .llmResponse(llmResponse)
            .timestamp(Instant.now())
            .build();
    }

}
