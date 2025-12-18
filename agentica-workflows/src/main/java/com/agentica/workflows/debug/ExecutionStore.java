package com.agentica.workflows.debug;

import com.agentica.core.domain.Event;
import com.agentica.core.enums.AgentType;
import com.agentica.core.event.workflow.AgentExecutionEvent;
import com.agentica.core.workflow.WorkflowPlan;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Simple storage for execution snapshots.
 * Does NOT know about the workflow execution - only stores data received from events.
 *
 * <p>This component is completely decoupled from the core execution logic.
 * Data is populated by WorkflowEventObserver which listens to domain events.
 */
@Slf4j
@Component
public class ExecutionStore {

    private static final int MAX_HISTORY = 50;

    private final Map<String, ExecutionSnapshot> currentExecutions = new ConcurrentHashMap<>();

    private final Deque<ExecutionSnapshot> history = new ConcurrentLinkedDeque<>();

    /**
     * Stores a new execution snapshot when a workflow starts.
     *
     * @param eventId the event ID being processed
     * @param plan the initial workflow plan
     */
    public void startExecution(String eventId, WorkflowPlan plan) {

        startExecution(eventId, plan, null);
    }

    /**
     * Stores a new execution snapshot when a workflow starts with event context.
     *
     * @param eventId the event ID being processed
     * @param plan the initial workflow plan
     * @param triggerEvent the event that triggered this workflow
     */
    public void startExecution(String eventId, WorkflowPlan plan, Event triggerEvent) {

        ExecutionSnapshot snapshot = ExecutionSnapshot.start(eventId, plan, triggerEvent);

        currentExecutions.put(eventId, snapshot);

        log.debug("Stored execution start, eventId: {}, workflowName: {}, hasEvent: {}",
            eventId, plan.workflowName(), triggerEvent != null);
    }

    /**
     * Updates snapshot when a node starts.
     *
     * @param eventId the event ID
     * @param nodeId the node ID
     * @param agentType the type of agent executing
     */
    public void nodeStarted(String eventId, String nodeId, AgentType agentType) {

        currentExecutions.computeIfPresent(eventId, (k, snapshot) -> {

            log.debug("Storing node start, eventId: {}, nodeId: {}", eventId, nodeId);

            return snapshot.nodeStarted(nodeId, agentType);
        });
    }

    /**
     * Updates snapshot when a node completes.
     *
     * @param eventId the event ID
     * @param nodeId the node ID
     * @param output the node's output
     * @param durationMs execution duration in milliseconds
     */
    public void nodeCompleted(String eventId, String nodeId, Object output, long durationMs) {

        currentExecutions.computeIfPresent(eventId, (k, snapshot) -> {

            log.debug("Storing node completion, eventId: {}, nodeId: {}", eventId, nodeId);

            return snapshot.nodeCompleted(nodeId, output, durationMs);
        });
    }

    /**
     * Updates snapshot when a node escalates.
     *
     * @param eventId the event ID
     * @param nodeId the node ID
     * @param reason the escalation reason
     * @param context additional context
     */
    public void nodeEscalated(String eventId, String nodeId, String reason, String context) {

        currentExecutions.computeIfPresent(eventId, (k, snapshot) -> {

            log.debug("Storing node escalation, eventId: {}, nodeId: {}", eventId, nodeId);

            return snapshot.nodeEscalated(nodeId, reason, context);
        });
    }

    /**
     * Updates snapshot when a workflow is replanned.
     *
     * @param eventId the event ID
     * @param newPlan the updated workflow plan
     */
    public void replanOccurred(String eventId, WorkflowPlan newPlan) {

        currentExecutions.computeIfPresent(eventId, (k, snapshot) -> {

            log.debug("Storing replan, eventId: {}, newVersion: {}",
                eventId, newPlan.version());

            return snapshot.replanOccurred(newPlan);
        });
    }

    /**
     * Updates snapshot when a workflow is replanned with full details.
     *
     * @param eventId the event ID
     * @param replanInfo the complete replan information
     */
    public void replanOccurred(String eventId, ReplanInfo replanInfo) {

        currentExecutions.computeIfPresent(eventId, (k, snapshot) -> {

            log.debug("Storing replan with details, eventId: {}, action: {}, triggerNode: {}",
                eventId, replanInfo.action(), replanInfo.triggerNodeId());

            return snapshot.replanOccurred(replanInfo);
        });
    }

    /**
     * Adds rich agent execution data to a node.
     *
     * @param eventId the event ID
     * @param nodeId the node ID
     * @param executionEvent the agent execution event with rich data
     */
    public void addAgentExecution(String eventId, String nodeId, AgentExecutionEvent executionEvent) {

        currentExecutions.computeIfPresent(eventId, (k, snapshot) -> {

            log.debug("Storing agent execution, eventId: {}, nodeId: {}, phase: {}",
                eventId, nodeId, executionEvent.phase());

            return snapshot.addAgentExecution(nodeId, executionEvent);
        });
    }

    /**
     * Marks execution as completed.
     *
     * @param eventId the event ID
     * @param state the final execution state
     */
    public void executionCompleted(String eventId, ExecutionSnapshot.ExecutionState state) {

        ExecutionSnapshot snapshot = currentExecutions.remove(eventId);

        if (snapshot != null) {

            ExecutionSnapshot completed = snapshot.complete(state);

            history.addFirst(completed);

            trimHistory();

            log.info("Stored execution completion, eventId: {}, state: {}, durationMs: {}",
                eventId, state, completed.totalDurationMs());
        }
    }

    /**
     * Marks execution as failed.
     *
     * @param eventId the event ID
     * @param errorMessage the error message
     */
    public void executionFailed(String eventId, String errorMessage) {

        ExecutionSnapshot snapshot = currentExecutions.remove(eventId);

        if (snapshot != null) {

            ExecutionSnapshot failed = snapshot.failed(errorMessage);

            history.addFirst(failed);

            trimHistory();

            log.warn("Stored execution failure, eventId: {}, error: {}",
                eventId, errorMessage);
        }
    }

    /**
     * Returns the current in-progress execution (most recent one).
     *
     * @return the current execution, or empty if none is running
     */
    public Optional<ExecutionSnapshot> getCurrent() {

        return currentExecutions.values().stream().findFirst();
    }

    /**
     * Returns a specific in-progress execution by event ID.
     *
     * @param eventId the event ID
     * @return the execution snapshot, or empty if not found
     */
    public Optional<ExecutionSnapshot> getCurrentByEventId(String eventId) {

        return Optional.ofNullable(currentExecutions.get(eventId));
    }

    /**
     * Returns the execution history (most recent first).
     *
     * @return immutable list of recent executions
     */
    public List<ExecutionSnapshot> getHistory() {

        return List.copyOf(history);
    }

    /**
     * Finds a specific execution by event ID (current or historical).
     *
     * @param eventId the event ID to find
     * @return the execution snapshot, or empty if not found
     */
    public Optional<ExecutionSnapshot> findByEventId(String eventId) {

        ExecutionSnapshot current = currentExecutions.get(eventId);

        if (current != null) {

            return Optional.of(current);
        }

        return history.stream()
            .filter(snapshot -> eventId.equals(snapshot.eventId()))
            .findFirst();
    }

    /**
     * Clears all execution data.
     * Primarily for testing purposes.
     */
    public void clear() {

        currentExecutions.clear();

        history.clear();

        log.debug("Execution store cleared");
    }

    private void trimHistory() {

        while (history.size() > MAX_HISTORY) {

            history.removeLast();
        }
    }

}
