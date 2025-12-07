package com.agentica.workflows.service;

import com.agentica.core.domain.Event;
import com.agentica.core.domain.Workflow;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing workflow lifecycle.
 */
public interface WorkflowService {

    /**
     * Processes an incoming event through the workflow pipeline.
     *
     * @param event the event to process
     * @return the created workflow if the event is actionable
     */
    Optional<Workflow> processEvent(Event event);

    /**
     * Retrieves a workflow by its ID.
     *
     * @param workflowId the workflow ID
     * @return the workflow if found
     */
    Optional<Workflow> getWorkflow(String workflowId);

    /**
     * Retrieves all workflows awaiting approval.
     *
     * @param tenantId the tenant ID
     * @return list of workflows awaiting approval
     */
    List<Workflow> getWorkflowsAwaitingApproval(String tenantId);

    /**
     * Approves a workflow for execution.
     *
     * @param workflowId the workflow ID
     * @param approvedBy the user who approved
     * @return the approved workflow
     */
    Workflow approveWorkflow(String workflowId, String approvedBy);

    /**
     * Rejects a workflow.
     *
     * @param workflowId the workflow ID
     * @param rejectedBy the user who rejected
     * @param reason the rejection reason
     * @return the rejected workflow
     */
    Workflow rejectWorkflow(String workflowId, String rejectedBy, String reason);

}
