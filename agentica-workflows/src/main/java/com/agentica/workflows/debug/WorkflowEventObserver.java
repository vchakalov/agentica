package com.agentica.workflows.debug;

import com.agentica.core.event.workflow.AgentExecutionEvent;
import com.agentica.core.event.workflow.NodeCompletedEvent;
import com.agentica.core.event.workflow.NodeEscalatedEvent;
import com.agentica.core.event.workflow.NodeStartedEvent;
import com.agentica.core.event.workflow.WorkflowCompletedEvent;
import com.agentica.core.event.workflow.WorkflowReplanEvent;
import com.agentica.core.event.workflow.WorkflowStartedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observes workflow domain events and builds execution snapshots.
 * This completely decouples the debug/observability layer from core execution logic.
 *
 * <p>The core workflow executor publishes domain events via Spring's ApplicationEventPublisher.
 * This observer listens to those events and builds the ExecutionSnapshot for the debug UI.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowEventObserver {

    private final ExecutionStore executionStore;

    @EventListener
    public void onWorkflowStarted(WorkflowStartedEvent event) {

        log.debug("Observed workflow started, eventId: {}, workflowName: {}, hasEvent: {}",
            event.eventId(), event.plan().workflowName(), event.triggerEvent() != null);

        executionStore.startExecution(event.eventId(), event.plan(), event.triggerEvent());
    }

    @EventListener
    public void onWorkflowCompleted(WorkflowCompletedEvent event) {

        log.debug("Observed workflow completed, eventId: {}, status: {}",
            event.eventId(), event.status());

        ExecutionSnapshot.ExecutionState state = switch (event.status()) {
            case COMPLETED -> ExecutionSnapshot.ExecutionState.COMPLETED;
            case ABORTED -> ExecutionSnapshot.ExecutionState.ABORTED;
            case FAILED -> ExecutionSnapshot.ExecutionState.FAILED;
        };

        if (event.errorMessage() != null) {

            executionStore.executionFailed(event.eventId(), event.errorMessage());

        } else {

            executionStore.executionCompleted(event.eventId(), state);
        }
    }

    @EventListener
    public void onWorkflowReplan(WorkflowReplanEvent event) {

        log.debug("Observed workflow replan, eventId: {}, replanNumber: {}, action: {}, triggerNode: {}",
            event.eventId(),
            event.replanNumber(),
            event.decision() != null ? event.decision().action() : "unknown",
            event.triggerNodeId());

        // Use escalation context from the event directly (now passed from executor)
        String escalationContext = event.escalationContext();

        // Fallback: try to get from node execution if not in event
        if (escalationContext == null) {

            escalationContext = executionStore.findByEventId(event.eventId())
                .flatMap(snapshot -> {

                    if (event.triggerNodeId() != null && snapshot.nodeExecutions() != null) {

                        NodeExecution nodeExec = snapshot.nodeExecutions().get(event.triggerNodeId());

                        if (nodeExec != null) {

                            return java.util.Optional.ofNullable(nodeExec.escalationContext());
                        }
                    }

                    return java.util.Optional.empty();
                })
                .orElse(null);
        }

        ReplanInfo replanInfo = ReplanInfo.from(
            event.replanNumber(),
            event.triggerNodeId(),
            event.escalationReason(),
            escalationContext,
            event.decision(),
            event.previousPlan(),
            event.newPlan(),
            event.llmPrompt(),
            event.llmResponse()
        );

        executionStore.replanOccurred(event.eventId(), replanInfo);
    }

    @EventListener
    public void onNodeStarted(NodeStartedEvent event) {

        log.debug("Observed node started, eventId: {}, nodeId: {}, agentType: {}",
            event.eventId(), event.nodeId(), event.agentType());

        executionStore.nodeStarted(event.eventId(), event.nodeId(), event.agentType());
    }

    @EventListener
    public void onNodeCompleted(NodeCompletedEvent event) {

        log.debug("Observed node completed, eventId: {}, nodeId: {}, durationMs: {}",
            event.eventId(), event.nodeId(), event.durationMs());

        executionStore.nodeCompleted(event.eventId(), event.nodeId(), event.output(), event.durationMs());
    }

    @EventListener
    public void onNodeEscalated(NodeEscalatedEvent event) {

        log.debug("Observed node escalated, eventId: {}, nodeId: {}, reason: {}",
            event.eventId(), event.nodeId(), event.escalationReason());

        executionStore.nodeEscalated(
            event.eventId(),
            event.nodeId(),
            event.escalationReason(),
            event.escalationContext()
        );
    }

    @EventListener
    public void onAgentExecution(AgentExecutionEvent event) {

        log.debug("Observed agent execution, eventId: {}, nodeId: {}, phase: {}",
            event.eventId(), event.nodeId(), event.phase());

        executionStore.addAgentExecution(event.eventId(), event.nodeId(), event);
    }

}
