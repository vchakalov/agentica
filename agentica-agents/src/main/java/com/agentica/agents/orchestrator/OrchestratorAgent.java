package com.agentica.agents.orchestrator;

import com.agentica.core.domain.Event;
import com.agentica.core.workflow.ReplanRequest;
import com.agentica.core.workflow.ReplanResult;
import com.agentica.core.workflow.WorkflowPlan;

/**
 * Agent responsible for designing dynamic workflow plans for incoming events.
 * Uses structured output to generate WorkflowPlan JSON that is validated and
 * built into LangGraph4j StateGraphs for execution.
 */
public interface OrchestratorAgent {

    /**
     * Designs a workflow plan for the given event using structured output.
     * The orchestrator LLM analyzes the event and outputs a complete WorkflowPlan
     * that can be built into a LangGraph4j StateGraph.
     *
     * @param event the event to design a workflow for
     * @return the designed WorkflowPlan
     */
    WorkflowPlan planWorkflow(Event event);

    /**
     * Replans a workflow based on agent escalation.
     * Called when an agent signals needsReplan during workflow execution.
     * The orchestrator analyzes the escalation context and decides how to modify the plan.
     *
     * @param request the replan request containing current state and escalation context
     * @return the replan result containing decision and LLM interaction details
     */
    ReplanResult replan(ReplanRequest request);

}
