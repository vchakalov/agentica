package com.agentica.core.domain;

import com.agentica.core.enums.WorkflowStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Workflow(

        String id,

        @NotBlank
        String tenantId,

        @NotBlank
        String eventId,

        String adkSessionId,

        @NotNull
        WorkflowStatus status,

        String name,

        String description,

        List<WorkflowStep> steps,

        int currentStepIndex,

        Instant createdAt,

        Instant approvedAt,

        String approvedBy,

        Instant completedAt,

        String errorMessage

) {

    /**
     * Creates a new draft workflow.
     */
    public static Workflow createDraft(String tenantId, String eventId, String name, String description, List<WorkflowStep> steps) {
        return Workflow.builder()
                .tenantId(tenantId)
                .eventId(eventId)
                .name(name)
                .description(description)
                .steps(steps)
                .status(WorkflowStatus.DRAFT)
                .currentStepIndex(0)
                .createdAt(Instant.now())
                .build();
    }

    /**
     * Returns a copy of this workflow with status updated to AWAITING_APPROVAL.
     */
    public Workflow submitForApproval() {
        return this.toBuilder()
                .status(WorkflowStatus.AWAITING_APPROVAL)
                .build();
    }

    /**
     * Returns a copy of this workflow with status updated to APPROVED.
     */
    public Workflow approve(String approvedBy) {
        return this.toBuilder()
                .status(WorkflowStatus.APPROVED)
                .approvedAt(Instant.now())
                .approvedBy(approvedBy)
                .build();
    }

    /**
     * Returns a copy of this workflow with status updated to REJECTED.
     */
    public Workflow reject(String rejectedBy, String reason) {
        return this.toBuilder()
                .status(WorkflowStatus.REJECTED)
                .errorMessage("Rejected by " + rejectedBy + ": " + reason)
                .build();
    }

    /**
     * Returns a copy of this workflow with status updated to EXECUTING.
     */
    public Workflow startExecution() {
        return this.toBuilder()
                .status(WorkflowStatus.EXECUTING)
                .build();
    }

    /**
     * Returns a copy of this workflow with status updated to COMPLETED.
     */
    public Workflow complete() {
        return this.toBuilder()
                .status(WorkflowStatus.COMPLETED)
                .completedAt(Instant.now())
                .build();
    }

    /**
     * Returns a copy of this workflow with status updated to FAILED.
     */
    public Workflow fail(String errorMessage) {
        return this.toBuilder()
                .status(WorkflowStatus.FAILED)
                .completedAt(Instant.now())
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * Advances to the next step.
     */
    public Workflow advanceStep() {
        return this.toBuilder()
                .currentStepIndex(currentStepIndex + 1)
                .build();
    }

    /**
     * Checks if all steps have been completed.
     */
    public boolean isAllStepsCompleted() {
        return currentStepIndex >= steps.size();
    }

}
