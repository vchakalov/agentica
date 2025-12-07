package com.agentica.agents.service;

import com.agentica.core.domain.Event;
import com.agentica.core.domain.Workflow;
import com.agentica.core.domain.WorkflowStep;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for AI agent operations.
 */
public interface AgentService {

    /**
     * Determines if an event is actionable using the filter agent.
     *
     * @param event the event to evaluate
     * @return true if the event requires action, false otherwise
     */
    boolean isEventActionable(Event event);

    /**
     * Creates a workflow plan for an actionable event using the orchestrator agent.
     *
     * @param event the actionable event
     * @return an optional workflow plan, empty if no plan could be created
     */
    Optional<Workflow> createWorkflowPlan(Event event);

    /**
     * Executes a single workflow step using the appropriate specialized agent.
     *
     * @param workflow the workflow being executed
     * @param step the step to execute
     * @return the result of the step execution
     */
    String executeWorkflowStep(Workflow workflow, WorkflowStep step);

    /**
     * Gets the list of available agent types.
     *
     * @return list of agent type names
     */
    List<String> getAvailableAgents();

}
