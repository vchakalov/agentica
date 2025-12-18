package com.agentica.core.event.workflow;

import java.time.Instant;

/**
 * Base interface for all workflow execution events.
 * These events are published during workflow execution and consumed
 * by observers (like the debugging dashboard) without coupling
 * the core execution logic to observability concerns.
 */
public sealed interface WorkflowEvent permits
    WorkflowStartedEvent,
    WorkflowCompletedEvent,
    WorkflowReplanEvent,
    NodeStartedEvent,
    NodeCompletedEvent,
    NodeEscalatedEvent,
    AgentExecutionEvent {

    /**
     * The event ID being processed by the workflow.
     */
    String eventId();

    /**
     * When this event occurred.
     */
    Instant timestamp();

}
