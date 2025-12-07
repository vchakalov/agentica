package com.agentica.agents.orchestrator;

import com.agentica.core.domain.Event;
import com.agentica.core.domain.WorkflowStep;

import java.util.List;

/**
 * Agent responsible for creating workflow execution plans
 * for actionable events.
 */
public interface OrchestratorAgent {

    /**
     * Creates a workflow plan for the given event.
     *
     * @param event the actionable event
     * @return a result containing the workflow plan
     */
    OrchestrationResult orchestrate(Event event);

    /**
     * Result of the orchestrator agent planning.
     */
    record OrchestrationResult(

            boolean success,

            String workflowName,

            String workflowDescription,

            List<WorkflowStep> steps,

            String errorMessage

    ) {

        /**
         * Creates a successful orchestration result.
         */
        public static OrchestrationResult success(String name, String description, List<WorkflowStep> steps) {
            return new OrchestrationResult(true, name, description, steps, null);
        }

        /**
         * Creates a failed orchestration result.
         */
        public static OrchestrationResult failure(String errorMessage) {
            return new OrchestrationResult(false, null, null, List.of(), errorMessage);
        }

    }

}
