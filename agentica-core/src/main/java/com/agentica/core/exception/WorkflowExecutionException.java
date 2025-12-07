package com.agentica.core.exception;

/**
 * Exception thrown when workflow execution fails.
 */
public class WorkflowExecutionException extends AgenticaException {

    private final String workflowId;

    public WorkflowExecutionException(String workflowId, String message) {
        super(message);
        this.workflowId = workflowId;
    }

    public WorkflowExecutionException(String workflowId, String message, Throwable cause) {
        super(message, cause);
        this.workflowId = workflowId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

}
