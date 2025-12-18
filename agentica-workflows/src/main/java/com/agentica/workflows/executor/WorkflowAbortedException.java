package com.agentica.workflows.executor;

/**
 * Exception thrown when a workflow is aborted by the orchestrator.
 */
public class WorkflowAbortedException extends RuntimeException {

    public WorkflowAbortedException(String reason) {

        super("Workflow aborted: " + reason);
    }

    public WorkflowAbortedException(String reason, Throwable cause) {

        super("Workflow aborted: " + reason, cause);
    }

}
