package com.agentica.core.event.workflow;

import com.agentica.core.enums.AgentType;

import lombok.Builder;

import java.time.Instant;

/**
 * Published when a workflow node completes execution successfully.
 */
@Builder
public record NodeCompletedEvent(

    String eventId,

    String nodeId,

    AgentType agentType,

    Object output,

    long durationMs,

    Instant timestamp

) implements WorkflowEvent {

    public static NodeCompletedEvent of(
            String eventId,
            String nodeId,
            AgentType agentType,
            Object output,
            long durationMs) {

        return NodeCompletedEvent.builder()
            .eventId(eventId)
            .nodeId(nodeId)
            .agentType(agentType)
            .output(output)
            .durationMs(durationMs)
            .timestamp(Instant.now())
            .build();
    }

}
