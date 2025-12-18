package com.agentica.core.event.workflow;

import lombok.Builder;

import java.time.Instant;

/**
 * Published when a workflow execution completes (success, abort, or failure).
 */
@Builder
public record WorkflowCompletedEvent(

    String eventId,

    ExecutionStatus status,

    String errorMessage,

    long totalDurationMs,

    int replanCount,

    Instant timestamp

) implements WorkflowEvent {

    public enum ExecutionStatus {
        COMPLETED,
        ABORTED,
        FAILED
    }

    public static WorkflowCompletedEvent completed(String eventId, long durationMs, int replanCount) {

        return WorkflowCompletedEvent.builder()
            .eventId(eventId)
            .status(ExecutionStatus.COMPLETED)
            .totalDurationMs(durationMs)
            .replanCount(replanCount)
            .timestamp(Instant.now())
            .build();
    }

    public static WorkflowCompletedEvent aborted(String eventId, String reason, long durationMs, int replanCount) {

        return WorkflowCompletedEvent.builder()
            .eventId(eventId)
            .status(ExecutionStatus.ABORTED)
            .errorMessage(reason)
            .totalDurationMs(durationMs)
            .replanCount(replanCount)
            .timestamp(Instant.now())
            .build();
    }

    public static WorkflowCompletedEvent failed(String eventId, String error, long durationMs, int replanCount) {

        return WorkflowCompletedEvent.builder()
            .eventId(eventId)
            .status(ExecutionStatus.FAILED)
            .errorMessage(error)
            .totalDurationMs(durationMs)
            .replanCount(replanCount)
            .timestamp(Instant.now())
            .build();
    }

}
