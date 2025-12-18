package com.agentica.core.event.workflow;

import com.agentica.core.domain.Event;
import com.agentica.core.workflow.WorkflowPlan;

import lombok.Builder;

import java.time.Instant;

/**
 * Published when a workflow execution begins.
 */
@Builder
public record WorkflowStartedEvent(

    String eventId,

    WorkflowPlan plan,

    Event triggerEvent,

    Instant timestamp

) implements WorkflowEvent {

    public static WorkflowStartedEvent of(String eventId, WorkflowPlan plan) {

        return WorkflowStartedEvent.builder()
            .eventId(eventId)
            .plan(plan)
            .timestamp(Instant.now())
            .build();
    }

    public static WorkflowStartedEvent of(String eventId, WorkflowPlan plan, Event triggerEvent) {

        return WorkflowStartedEvent.builder()
            .eventId(eventId)
            .plan(plan)
            .triggerEvent(triggerEvent)
            .timestamp(Instant.now())
            .build();
    }

}
