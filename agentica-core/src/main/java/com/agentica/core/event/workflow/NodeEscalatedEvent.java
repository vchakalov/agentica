package com.agentica.core.event.workflow;

import com.agentica.core.enums.AgentType;
import java.time.Instant;
import lombok.Builder;

@Builder
public record NodeEscalatedEvent(

    String eventId,

    String nodeId,

    AgentType agentType,

    String escalationReason,

    String escalationContext,

    long durationMs,

    Instant timestamp

) implements WorkflowEvent {

  public static NodeEscalatedEvent of(
      final String eventId,
      final String nodeId,
      final AgentType agentType,
      final String escalationReason,
      final String escalationContext,
      final long durationMs) {

    return NodeEscalatedEvent.builder()
        .eventId(eventId)
        .nodeId(nodeId)
        .agentType(agentType)
        .escalationReason(escalationReason)
        .escalationContext(escalationContext)
        .durationMs(durationMs)
        .timestamp(Instant.now())
        .build();
  }
}
