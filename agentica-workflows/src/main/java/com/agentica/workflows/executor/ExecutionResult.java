package com.agentica.workflows.executor;

import com.agentica.core.workflow.WorkflowPlan;
import com.agentica.core.state.AgenticaState;

import lombok.Builder;

import java.util.List;

/**
 * Result of workflow execution.
 * Contains final state, plan history for audit, and completion status.
 */
@Builder(toBuilder = true)
public record ExecutionResult(

    ExecutionStatus status,

    AgenticaState finalState,

    List<WorkflowPlan> planHistory,

    String errorMessage

) {

    public enum ExecutionStatus {
        COMPLETED,
        ABORTED,
        FAILED
    }

    /**
     * Creates a successful completion result.
     *
     * @param state the final state after workflow completion
     * @param planHistory the history of all plan versions
     * @return the execution result
     */
    public static ExecutionResult completed(AgenticaState state, List<WorkflowPlan> planHistory) {

        return ExecutionResult.builder()
            .status(ExecutionStatus.COMPLETED)
            .finalState(state)
            .planHistory(planHistory)
            .build();
    }

    /**
     * Creates an aborted result.
     *
     * @param reason the reason for abortion
     * @param planHistory the history of all plan versions
     * @return the execution result
     */
    public static ExecutionResult aborted(String reason, List<WorkflowPlan> planHistory) {

        return ExecutionResult.builder()
            .status(ExecutionStatus.ABORTED)
            .errorMessage(reason)
            .planHistory(planHistory)
            .build();
    }

    /**
     * Creates a failed result.
     *
     * @param errorMessage the error message
     * @param planHistory the history of all plan versions
     * @return the execution result
     */
    public static ExecutionResult failed(String errorMessage, List<WorkflowPlan> planHistory) {

        return ExecutionResult.builder()
            .status(ExecutionStatus.FAILED)
            .errorMessage(errorMessage)
            .planHistory(planHistory)
            .build();
    }

    /**
     * Checks if the workflow completed successfully.
     *
     * @return true if completed
     */
    public boolean isCompleted() {

        return status == ExecutionStatus.COMPLETED;
    }

    /**
     * Checks if the workflow was aborted.
     *
     * @return true if aborted
     */
    public boolean isAborted() {

        return status == ExecutionStatus.ABORTED;
    }

    /**
     * Checks if the workflow failed.
     *
     * @return true if failed
     */
    public boolean isFailed() {

        return status == ExecutionStatus.FAILED;
    }

}
