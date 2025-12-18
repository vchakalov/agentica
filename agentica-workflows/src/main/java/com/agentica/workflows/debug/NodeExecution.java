package com.agentica.workflows.debug;

import com.agentica.core.enums.AgentType;
import com.agentica.core.event.workflow.AgentExecutionEvent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Records the execution details of a single workflow node.
 * Includes rich agent execution data (LLM interactions, tool calls).
 */
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record NodeExecution(

    String nodeId,

    AgentType agentType,

    NodeStatus status,

    Instant startedAt,

    Instant completedAt,

    long durationMs,

    Object output,

    String escalationReason,

    String escalationContext,

    List<AgentExecutionEvent> agentExecutions

) {

    public enum NodeStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        ESCALATED,
        FAILED
    }

    /**
     * Creates a new node execution in RUNNING status.
     */
    public static NodeExecution started(String nodeId, AgentType agentType) {

        return NodeExecution.builder()
            .nodeId(nodeId)
            .agentType(agentType)
            .status(NodeStatus.RUNNING)
            .startedAt(Instant.now())
            .agentExecutions(new ArrayList<>())
            .build();
    }

    /**
     * Updates this execution to COMPLETED status.
     */
    public NodeExecution completed(Object output, long durationMs) {

        return this.toBuilder()
            .status(NodeStatus.COMPLETED)
            .completedAt(Instant.now())
            .durationMs(durationMs)
            .output(output)
            .build();
    }

    /**
     * Updates this execution to ESCALATED status.
     */
    public NodeExecution escalated(String reason, String context) {

        return this.toBuilder()
            .status(NodeStatus.ESCALATED)
            .completedAt(Instant.now())
            .durationMs(calculateDuration())
            .escalationReason(reason)
            .escalationContext(context)
            .build();
    }

    /**
     * Updates this execution to FAILED status.
     */
    public NodeExecution failed(String reason) {

        return this.toBuilder()
            .status(NodeStatus.FAILED)
            .completedAt(Instant.now())
            .durationMs(calculateDuration())
            .escalationReason(reason)
            .build();
    }

    /**
     * Adds rich agent execution data (LLM interaction, tool call, etc).
     */
    public NodeExecution addAgentExecution(AgentExecutionEvent executionEvent) {

        List<AgentExecutionEvent> updatedExecutions = new ArrayList<>(
            this.agentExecutions != null ? this.agentExecutions : new ArrayList<>()
        );

        updatedExecutions.add(executionEvent);

        return this.toBuilder()
            .agentExecutions(updatedExecutions)
            .build();
    }

    private long calculateDuration() {

        if (startedAt == null) {

            return 0;
        }

        return Instant.now().toEpochMilli() - startedAt.toEpochMilli();
    }

}
