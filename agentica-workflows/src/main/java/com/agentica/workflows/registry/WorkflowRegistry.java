package com.agentica.workflows.registry;

import com.agentica.core.state.AgenticaState;
import com.agentica.core.workflow.WorkflowPlan;

import lombok.extern.slf4j.Slf4j;

import org.bsc.langgraph4j.StateGraph;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Registry for tracking the latest workflow graph for visualization.
 * Uses a simple "always show latest" approach - keeps only one graph reference.
 */
@Slf4j
@Component
public class WorkflowRegistry {

    private volatile StateGraph<AgenticaState> latestGraph;

    private volatile WorkflowPlan latestPlan;

    private volatile String latestEventId;

    /**
     * Updates the registry with the latest workflow.
     * Called by WorkflowBuilder whenever a new graph is built.
     *
     * @param eventId the event ID this workflow is processing
     * @param plan the workflow plan
     * @param graph the built StateGraph (before compilation)
     */
    public synchronized void updateLatest(String eventId, WorkflowPlan plan,
                                          StateGraph<AgenticaState> graph) {

        this.latestEventId = eventId;
        this.latestPlan = plan;
        this.latestGraph = graph;

        log.info("Updated latest workflow for visualization, eventId: {}, workflowName: {}, nodes: {}",
            eventId, plan.workflowName(), plan.nodes().size());
    }

    /**
     * Returns the latest StateGraph for visualization.
     */
    public Optional<StateGraph<AgenticaState>> getLatestGraph() {

        return Optional.ofNullable(latestGraph);
    }

    /**
     * Returns the latest WorkflowPlan.
     */
    public Optional<WorkflowPlan> getLatestPlan() {

        return Optional.ofNullable(latestPlan);
    }

    /**
     * Returns the event ID of the latest workflow.
     */
    public Optional<String> getLatestEventId() {

        return Optional.ofNullable(latestEventId);
    }

    /**
     * Checks if any workflow has been registered.
     */
    public boolean hasWorkflow() {

        return latestGraph != null;
    }

}
