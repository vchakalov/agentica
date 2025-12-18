package com.agentica.core.event.workflow;

import com.agentica.core.enums.AgentType;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Published during agent node execution to capture rich LLM interaction details.
 * Contains all data about the LLM request/response cycle for debugging.
 */
@Builder
public record AgentExecutionEvent(

    String eventId,

    String nodeId,

    AgentType agentType,

    String agentName,

    String instruction,

    Map<String, Object> config,

    LlmInteraction llmInteraction,

    List<ToolCall> toolCalls,

    Object output,

    String error,

    ExecutionPhase phase,

    Instant timestamp

) implements WorkflowEvent {

    public enum ExecutionPhase {
        LLM_REQUEST_SENT,
        LLM_RESPONSE_RECEIVED,
        TOOL_CALL_STARTED,
        TOOL_CALL_COMPLETED,
        EXECUTION_COMPLETED,
        EXECUTION_FAILED
    }

    /**
     * Captures the full LLM interaction details.
     */
    @Builder
    public record LlmInteraction(

        String model,

        String systemPrompt,

        List<Message> messages,

        String rawRequest,

        String rawResponse,

        int inputTokens,

        int outputTokens,

        long latencyMs

    ) {}

    /**
     * Represents a message in the LLM conversation.
     */
    @Builder
    public record Message(

        String role,

        String content

    ) {}

    /**
     * Captures tool/function call details.
     */
    @Builder
    public record ToolCall(

        String toolName,

        String toolInput,

        String toolOutput,

        long durationMs,

        boolean success,

        String error

    ) {}

    public static AgentExecutionEvent llmRequestSent(
            String eventId,
            String nodeId,
            AgentType agentType,
            String agentName,
            String instruction,
            Map<String, Object> config,
            LlmInteraction interaction) {

        return AgentExecutionEvent.builder()
            .eventId(eventId)
            .nodeId(nodeId)
            .agentType(agentType)
            .agentName(agentName)
            .instruction(instruction)
            .config(config)
            .llmInteraction(interaction)
            .phase(ExecutionPhase.LLM_REQUEST_SENT)
            .timestamp(Instant.now())
            .build();
    }

    public static AgentExecutionEvent llmResponseReceived(
            String eventId,
            String nodeId,
            AgentType agentType,
            String agentName,
            LlmInteraction interaction) {

        return AgentExecutionEvent.builder()
            .eventId(eventId)
            .nodeId(nodeId)
            .agentType(agentType)
            .agentName(agentName)
            .llmInteraction(interaction)
            .phase(ExecutionPhase.LLM_RESPONSE_RECEIVED)
            .timestamp(Instant.now())
            .build();
    }

    public static AgentExecutionEvent toolCallStarted(
            String eventId,
            String nodeId,
            AgentType agentType,
            String toolName,
            String toolInput) {

        return AgentExecutionEvent.builder()
            .eventId(eventId)
            .nodeId(nodeId)
            .agentType(agentType)
            .toolCalls(List.of(ToolCall.builder()
                .toolName(toolName)
                .toolInput(toolInput)
                .build()))
            .phase(ExecutionPhase.TOOL_CALL_STARTED)
            .timestamp(Instant.now())
            .build();
    }

    public static AgentExecutionEvent toolCallCompleted(
            String eventId,
            String nodeId,
            AgentType agentType,
            ToolCall toolCall) {

        return AgentExecutionEvent.builder()
            .eventId(eventId)
            .nodeId(nodeId)
            .agentType(agentType)
            .toolCalls(List.of(toolCall))
            .phase(ExecutionPhase.TOOL_CALL_COMPLETED)
            .timestamp(Instant.now())
            .build();
    }

    public static AgentExecutionEvent completed(
            String eventId,
            String nodeId,
            AgentType agentType,
            String agentName,
            Object output,
            List<ToolCall> toolCalls) {

        return AgentExecutionEvent.builder()
            .eventId(eventId)
            .nodeId(nodeId)
            .agentType(agentType)
            .agentName(agentName)
            .output(output)
            .toolCalls(toolCalls)
            .phase(ExecutionPhase.EXECUTION_COMPLETED)
            .timestamp(Instant.now())
            .build();
    }

    public static AgentExecutionEvent failed(
            String eventId,
            String nodeId,
            AgentType agentType,
            String agentName,
            String error) {

        return AgentExecutionEvent.builder()
            .eventId(eventId)
            .nodeId(nodeId)
            .agentType(agentType)
            .agentName(agentName)
            .error(error)
            .phase(ExecutionPhase.EXECUTION_FAILED)
            .timestamp(Instant.now())
            .build();
    }

}
