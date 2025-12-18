package com.agentica.core.workflow;

/**
 * Actions that can be taken during a workflow replan.
 */
public enum ReplanAction {

    /**
     * Replace the entire workflow plan with a new one.
     */
    MODIFY_PLAN,

    /**
     * Add new nodes and edges to the existing plan.
     */
    ADD_NODES,

    /**
     * Retry the current step with additional guidance.
     */
    RETRY_WITH_GUIDANCE,

    /**
     * Abort the workflow execution.
     */
    ABORT

}
