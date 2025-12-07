package com.agentica.workflows.executor;

import com.agentica.core.domain.Workflow;

/**
 * Executor for running approved workflows.
 */
public interface WorkflowExecutor {

    /**
     * Executes an approved workflow.
     *
     * @param workflow the workflow to execute
     * @return the completed workflow with results
     */
    Workflow execute(Workflow workflow);

    /**
     * Executes a single step of a workflow.
     *
     * @param workflow the workflow
     * @param stepIndex the index of the step to execute
     * @return the updated workflow
     */
    Workflow executeStep(Workflow workflow, int stepIndex);

}
