package com.agentica.core.enums;

/**
 * Status of a workflow execution.
 */
public enum WorkflowStatus {

    /**
     * Workflow plan has been created but not yet submitted for approval.
     */
    DRAFT,

    /**
     * Workflow is awaiting human approval.
     */
    AWAITING_APPROVAL,

    /**
     * Workflow has been approved and is ready for execution.
     */
    APPROVED,

    /**
     * Workflow was rejected by the human reviewer.
     */
    REJECTED,

    /**
     * Workflow is currently being executed.
     */
    EXECUTING,

    /**
     * Workflow execution completed successfully.
     */
    COMPLETED,

    /**
     * Workflow execution failed.
     */
    FAILED,

    /**
     * Workflow was cancelled.
     */
    CANCELLED

}
