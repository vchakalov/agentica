package com.agentica.workflows.debug;

import com.agentica.core.domain.Event;
import com.agentica.core.enums.AgentType;
import com.agentica.core.event.workflow.AgentExecutionEvent;
import com.agentica.core.workflow.WorkflowPlan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Captures the complete execution state of a workflow.
 * Immutable - each mutation creates a new snapshot.
 */
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExecutionSnapshot(

    String eventId,

    Event triggerEvent,

    WorkflowPlan plan,

    ExecutionState state,

    String activeNodeId,

    Instant startedAt,

    Instant completedAt,

    long totalDurationMs,

    Map<String, NodeExecution> nodeExecutions,

    List<String> executionOrder,

    int replanCount,

    String errorMessage,

    List<WorkflowPlan> planHistory,

    List<ReplanInfo> replanHistory

) {

    public enum ExecutionState {
        RUNNING,
        COMPLETED,
        ABORTED,
        FAILED
    }

    /**
     * Creates a new execution snapshot when a workflow starts.
     */
    public static ExecutionSnapshot start(String eventId, WorkflowPlan plan) {

        return start(eventId, plan, null);
    }

    /**
     * Creates a new execution snapshot when a workflow starts with event context.
     */
    public static ExecutionSnapshot start(String eventId, WorkflowPlan plan, Event triggerEvent) {

        List<WorkflowPlan> history = new ArrayList<>();

        history.add(plan);

        return ExecutionSnapshot.builder()
            .eventId(eventId)
            .triggerEvent(triggerEvent)
            .plan(plan)
            .state(ExecutionState.RUNNING)
            .startedAt(Instant.now())
            .nodeExecutions(new LinkedHashMap<>())
            .executionOrder(new ArrayList<>())
            .replanCount(0)
            .planHistory(history)
            .replanHistory(new ArrayList<>())
            .build();
    }

    /**
     * Records that a node has started execution.
     */
    public ExecutionSnapshot nodeStarted(String nodeId, AgentType agentType) {

        Map<String, NodeExecution> updatedExecutions = new LinkedHashMap<>(this.nodeExecutions);

        updatedExecutions.put(nodeId, NodeExecution.started(nodeId, agentType));

        List<String> updatedOrder = new ArrayList<>(this.executionOrder);

        updatedOrder.add(nodeId);

        return this.toBuilder()
            .activeNodeId(nodeId)
            .nodeExecutions(updatedExecutions)
            .executionOrder(updatedOrder)
            .build();
    }

    /**
     * Records that a node has completed successfully.
     */
    public ExecutionSnapshot nodeCompleted(String nodeId, Object output, long durationMs) {

        Map<String, NodeExecution> updatedExecutions = new LinkedHashMap<>(this.nodeExecutions);

        NodeExecution existing = updatedExecutions.get(nodeId);

        if (existing != null) {

            updatedExecutions.put(nodeId, existing.completed(output, durationMs));
        }

        return this.toBuilder()
            .activeNodeId(null)
            .nodeExecutions(updatedExecutions)
            .build();
    }

    /**
     * Records that a node has escalated to the orchestrator.
     */
    public ExecutionSnapshot nodeEscalated(String nodeId, String reason, String context) {

        Map<String, NodeExecution> updatedExecutions = new LinkedHashMap<>(this.nodeExecutions);

        NodeExecution existing = updatedExecutions.get(nodeId);

        if (existing != null) {

            updatedExecutions.put(nodeId, existing.escalated(reason, context));
        }

        return this.toBuilder()
            .activeNodeId(null)
            .nodeExecutions(updatedExecutions)
            .build();
    }

    /**
     * Records a replan event with full details.
     */
    public ExecutionSnapshot replanOccurred(ReplanInfo replanInfo) {

        List<WorkflowPlan> updatedPlanHistory = new ArrayList<>(
            this.planHistory != null ? this.planHistory : new ArrayList<>()
        );

        updatedPlanHistory.add(replanInfo.newPlan());

        List<ReplanInfo> updatedReplanHistory = new ArrayList<>(
            this.replanHistory != null ? this.replanHistory : new ArrayList<>()
        );

        updatedReplanHistory.add(replanInfo);

        return this.toBuilder()
            .plan(replanInfo.newPlan())
            .replanCount(this.replanCount + 1)
            .planHistory(updatedPlanHistory)
            .replanHistory(updatedReplanHistory)
            .build();
    }

    /**
     * Records a replan event (simple version for backwards compatibility).
     */
    public ExecutionSnapshot replanOccurred(WorkflowPlan newPlan) {

        ReplanInfo info = ReplanInfo.builder()
            .replanNumber(this.replanCount + 1)
            .newPlan(newPlan)
            .previousPlan(this.plan)
            .build();

        return replanOccurred(info);
    }

    /**
     * Adds rich agent execution data to a node.
     */
    public ExecutionSnapshot addAgentExecution(String nodeId, AgentExecutionEvent executionEvent) {

        Map<String, NodeExecution> updatedExecutions = new LinkedHashMap<>(this.nodeExecutions);

        NodeExecution existing = updatedExecutions.get(nodeId);

        if (existing != null) {

            updatedExecutions.put(nodeId, existing.addAgentExecution(executionEvent));
        }

        return this.toBuilder()
            .nodeExecutions(updatedExecutions)
            .build();
    }

    /**
     * Marks the execution as completed.
     */
    public ExecutionSnapshot complete(ExecutionState finalState) {

        Instant now = Instant.now();

        long duration = now.toEpochMilli() - this.startedAt.toEpochMilli();

        return this.toBuilder()
            .state(finalState)
            .activeNodeId(null)
            .completedAt(now)
            .totalDurationMs(duration)
            .build();
    }

    /**
     * Marks the execution as failed with an error message.
     */
    public ExecutionSnapshot failed(String error) {

        Instant now = Instant.now();

        long duration = now.toEpochMilli() - this.startedAt.toEpochMilli();

        return this.toBuilder()
            .state(ExecutionState.FAILED)
            .activeNodeId(null)
            .completedAt(now)
            .totalDurationMs(duration)
            .errorMessage(error)
            .build();
    }

}
