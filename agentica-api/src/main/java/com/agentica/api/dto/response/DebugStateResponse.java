package com.agentica.api.dto.response;

import com.agentica.core.domain.Event;
import com.agentica.core.enums.AgentType;
import com.agentica.core.event.workflow.AgentExecutionEvent;
import com.agentica.core.workflow.ReplanAction;
import com.agentica.workflows.debug.ExecutionSnapshot;
import com.agentica.workflows.debug.NodeExecution;
import com.agentica.workflows.debug.ReplanInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for the debug dashboard state endpoint.
 * Contains all information needed to render the debugging UI.
 */
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record DebugStateResponse(

    /**
     * Whether there is an active execution in progress.
     */
    boolean hasActiveExecution,

    /**
     * Current execution details (null if none active).
     */
    ExecutionInfo currentExecution,

    /**
     * The trigger event that started this workflow.
     */
    EventInfo triggerEvent,

    /**
     * Graph structure for rendering.
     */
    GraphInfo graph,

    /**
     * ID of the currently executing node (null if none).
     */
    String activeNodeId,

    /**
     * Ordered list of node executions for timeline display.
     */
    List<NodeExecutionInfo> timeline,

    /**
     * List of replan events with full details.
     */
    List<ReplanInfoDto> replans,

    /**
     * Summary of recent execution history.
     */
    List<ExecutionSummary> recentHistory,

    /**
     * Timestamp when this state was generated.
     */
    Instant timestamp

) {

    @Builder(toBuilder = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExecutionInfo(

        String eventId,

        String workflowName,

        String workflowId,

        String state,

        Instant startedAt,

        Instant completedAt,

        long durationMs,

        int replanCount,

        String errorMessage

    ) {

        public static ExecutionInfo from(ExecutionSnapshot snapshot) {

            if (snapshot == null) {

                return null;
            }

            long duration = snapshot.totalDurationMs();

            if (duration == 0 && snapshot.startedAt() != null) {

                duration = Instant.now().toEpochMilli() - snapshot.startedAt().toEpochMilli();
            }

            return ExecutionInfo.builder()
                .eventId(snapshot.eventId())
                .workflowName(snapshot.plan() != null ? snapshot.plan().workflowName() : null)
                .workflowId(snapshot.plan() != null ? snapshot.plan().workflowId() : null)
                .state(snapshot.state() != null ? snapshot.state().name() : null)
                .startedAt(snapshot.startedAt())
                .completedAt(snapshot.completedAt())
                .durationMs(duration)
                .replanCount(snapshot.replanCount())
                .errorMessage(snapshot.errorMessage())
                .build();
        }
    }

    @Builder(toBuilder = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GraphInfo(

        List<GraphNode> nodes,

        List<GraphEdge> edges

    ) {}

    @Builder(toBuilder = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GraphNode(

        String id,

        AgentType agentType,

        String description,

        String status

    ) {}

    @Builder(toBuilder = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GraphEdge(

        String from,

        String to,

        String type,

        String label

    ) {}

    @Builder(toBuilder = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NodeExecutionInfo(

        String nodeId,

        AgentType agentType,

        String status,

        Instant startedAt,

        Instant completedAt,

        long durationMs,

        Object output,

        String escalationReason,

        String escalationContext,

        List<AgentExecutionDetail> agentExecutions

    ) {

        public static NodeExecutionInfo from(NodeExecution execution) {

            if (execution == null) {

                return null;
            }

            List<AgentExecutionDetail> agentDetails = null;

            if (execution.agentExecutions() != null) {

                agentDetails = execution.agentExecutions().stream()
                    .map(AgentExecutionDetail::from)
                    .toList();
            }

            return NodeExecutionInfo.builder()
                .nodeId(execution.nodeId())
                .agentType(execution.agentType())
                .status(execution.status() != null ? execution.status().name() : null)
                .startedAt(execution.startedAt())
                .completedAt(execution.completedAt())
                .durationMs(execution.durationMs())
                .output(execution.output())
                .escalationReason(execution.escalationReason())
                .escalationContext(execution.escalationContext())
                .agentExecutions(agentDetails)
                .build();
        }
    }

    /**
     * Rich agent execution detail including LLM interactions and tool calls.
     */
    @Builder(toBuilder = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AgentExecutionDetail(

        String nodeId,

        AgentType agentType,

        String agentName,

        String instruction,

        Map<String, Object> config,

        String phase,

        LlmInteractionInfo llmInteraction,

        List<ToolCallInfo> toolCalls,

        Object output,

        String error,

        Instant timestamp

    ) {

        public static AgentExecutionDetail from(AgentExecutionEvent event) {

            if (event == null) {

                return null;
            }

            LlmInteractionInfo llmInfo = null;

            if (event.llmInteraction() != null) {

                var interaction = event.llmInteraction();

                List<MessageInfo> messages = null;

                if (interaction.messages() != null) {

                    messages = interaction.messages().stream()
                        .map(m -> new MessageInfo(m.role(), m.content()))
                        .toList();
                }

                llmInfo = new LlmInteractionInfo(
                    interaction.model(),
                    interaction.systemPrompt(),
                    messages,
                    interaction.rawRequest(),
                    interaction.rawResponse(),
                    interaction.inputTokens(),
                    interaction.outputTokens(),
                    interaction.latencyMs()
                );
            }

            List<ToolCallInfo> toolCallInfos = null;

            if (event.toolCalls() != null) {

                toolCallInfos = event.toolCalls().stream()
                    .map(tc -> new ToolCallInfo(
                        tc.toolName(),
                        tc.toolInput(),
                        tc.toolOutput(),
                        tc.durationMs(),
                        tc.success(),
                        tc.error()
                    ))
                    .toList();
            }

            return AgentExecutionDetail.builder()
                .nodeId(event.nodeId())
                .agentType(event.agentType())
                .agentName(event.agentName())
                .instruction(event.instruction())
                .config(event.config())
                .phase(event.phase() != null ? event.phase().name() : null)
                .llmInteraction(llmInfo)
                .toolCalls(toolCallInfos)
                .output(event.output())
                .error(event.error())
                .timestamp(event.timestamp())
                .build();
        }
    }

    /**
     * LLM interaction details.
     */
    @Builder(toBuilder = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LlmInteractionInfo(

        String model,

        String systemPrompt,

        List<MessageInfo> messages,

        String rawRequest,

        String rawResponse,

        int inputTokens,

        int outputTokens,

        long latencyMs

    ) {}

    /**
     * A message in the LLM conversation.
     */
    @Builder(toBuilder = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MessageInfo(

        String role,

        String content

    ) {}

    /**
     * Tool call details.
     */
    @Builder(toBuilder = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ToolCallInfo(

        String toolName,

        String toolInput,

        String toolOutput,

        long durationMs,

        boolean success,

        String error

    ) {}

    @Builder(toBuilder = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExecutionSummary(

        String eventId,

        String workflowName,

        String state,

        Instant startedAt,

        Instant completedAt,

        long durationMs,

        int nodeCount,

        int replanCount

    ) {

        public static ExecutionSummary from(ExecutionSnapshot snapshot) {

            if (snapshot == null) {

                return null;
            }

            return ExecutionSummary.builder()
                .eventId(snapshot.eventId())
                .workflowName(snapshot.plan() != null ? snapshot.plan().workflowName() : null)
                .state(snapshot.state() != null ? snapshot.state().name() : null)
                .startedAt(snapshot.startedAt())
                .completedAt(snapshot.completedAt())
                .durationMs(snapshot.totalDurationMs())
                .nodeCount(snapshot.executionOrder() != null ? snapshot.executionOrder().size() : 0)
                .replanCount(snapshot.replanCount())
                .build();
        }
    }

    /**
     * Information about the trigger event.
     */
    @Builder(toBuilder = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EventInfo(

        String id,

        String eventType,

        String source,

        Map<String, Object> payload,

        String externalId,

        String category,

        Integer priority,

        Instant receivedAt

    ) {

        public static EventInfo from(Event event) {

            if (event == null) {

                return null;
            }

            return EventInfo.builder()
                .id(event.id())
                .eventType(event.eventType())
                .source(event.source())
                .payload(event.payload())
                .externalId(event.externalId())
                .category(event.category())
                .priority(event.priority())
                .receivedAt(event.receivedAt())
                .build();
        }
    }

    /**
     * Detailed information about a workflow replan event.
     */
    @Builder(toBuilder = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReplanInfoDto(

        int replanNumber,

        String triggerNodeId,

        String escalationReason,

        String escalationContext,

        String action,

        String guidance,

        String abortReason,

        String resumeFrom,

        /**
         * The workflow plan before the replan occurred.
         */
        WorkflowPlanDto previousPlan,

        /**
         * The workflow plan after the replan (may be same as previous for RETRY_WITH_GUIDANCE).
         */
        WorkflowPlanDto newPlan,

        /**
         * The prompt sent to the orchestrator LLM.
         */
        String llmPrompt,

        /**
         * The raw response from the orchestrator LLM.
         */
        String llmResponse,

        Instant timestamp

    ) {

        public static ReplanInfoDto from(ReplanInfo info) {

            if (info == null) {

                return null;
            }

            return ReplanInfoDto.builder()
                .replanNumber(info.replanNumber())
                .triggerNodeId(info.triggerNodeId())
                .escalationReason(info.escalationReason())
                .escalationContext(info.escalationContext())
                .action(info.action() != null ? info.action().name() : null)
                .guidance(info.guidance())
                .abortReason(info.abortReason())
                .resumeFrom(info.resumeFrom())
                .previousPlan(WorkflowPlanDto.from(info.previousPlan()))
                .newPlan(WorkflowPlanDto.from(info.newPlan()))
                .llmPrompt(info.llmPrompt())
                .llmResponse(info.llmResponse())
                .timestamp(info.timestamp())
                .build();
        }
    }

    /**
     * DTO for workflow plan comparison in replans.
     */
    @Builder(toBuilder = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WorkflowPlanDto(

        String workflowId,

        String workflowName,

        String description,

        int version,

        List<WorkflowNodeDto> nodes,

        List<WorkflowEdgeDto> edges,

        String entryPoint

    ) {

        public static WorkflowPlanDto from(com.agentica.core.workflow.WorkflowPlan plan) {

            if (plan == null) {

                return null;
            }

            List<WorkflowNodeDto> nodeDtos = plan.nodes() != null
                ? plan.nodes().stream().map(WorkflowNodeDto::from).toList()
                : List.of();

            List<WorkflowEdgeDto> edgeDtos = plan.edges() != null
                ? plan.edges().stream().map(WorkflowEdgeDto::from).toList()
                : List.of();

            return WorkflowPlanDto.builder()
                .workflowId(plan.workflowId())
                .workflowName(plan.workflowName())
                .description(plan.description())
                .version(plan.version())
                .nodes(nodeDtos)
                .edges(edgeDtos)
                .entryPoint(plan.entryPoint())
                .build();
        }
    }

    /**
     * DTO for workflow node in plan comparison.
     */
    @Builder(toBuilder = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WorkflowNodeDto(

        String id,

        String agentType,

        String description,

        String instruction

    ) {

        public static WorkflowNodeDto from(com.agentica.core.workflow.WorkflowNode node) {

            if (node == null) {

                return null;
            }

            return WorkflowNodeDto.builder()
                .id(node.id())
                .agentType(node.agentType() != null ? node.agentType().name() : null)
                .description(node.description())
                .instruction(node.instruction())
                .build();
        }
    }

    /**
     * DTO for workflow edge in plan comparison.
     */
    @Builder(toBuilder = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WorkflowEdgeDto(

        String from,

        String to,

        String type,

        String conditionKey

    ) {

        public static WorkflowEdgeDto from(com.agentica.core.workflow.WorkflowEdge edge) {

            if (edge == null) {

                return null;
            }

            return WorkflowEdgeDto.builder()
                .from(edge.from())
                .to(edge.to())
                .type(edge.type() != null ? edge.type().name() : null)
                .conditionKey(edge.condition() != null ? edge.condition().stateKey() : null)
                .build();
        }
    }

}
