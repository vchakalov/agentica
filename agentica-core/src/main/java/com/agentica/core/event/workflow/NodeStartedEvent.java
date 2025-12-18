package com.agentica.core.event.workflow;

import com.agentica.core.enums.AgentType;

import lombok.Builder;

import java.time.Instant;
import java.util.Map;

/**
 * Published when a workflow node begins execution.
 */
@Builder
public record NodeStartedEvent(

    String eventId,

    String nodeId,

    AgentType agentType,

    String instruction,

    Map<String, Object> config,

    Instant timestamp

) implements WorkflowEvent {

    public static NodeStartedEvent of(
            String eventId,
            String nodeId,
            AgentType agentType,
            String instruction,
            Map<String, Object> config) {

        return NodeStartedEvent.builder()
            .eventId(eventId)
            .nodeId(nodeId)
            .agentType(agentType)
            .instruction(instruction)
            .config(config)
            .timestamp(Instant.now())
            .build();
    }

}
