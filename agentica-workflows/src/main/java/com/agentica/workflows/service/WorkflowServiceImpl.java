package com.agentica.workflows.service;

import com.agentica.agents.orchestrator.OrchestratorAgent;
import com.agentica.core.workflow.WorkflowPlan;
import com.agentica.core.domain.Event;
import com.agentica.core.domain.Workflow;
import com.agentica.core.enums.WorkflowStatus;
import com.agentica.infrastructure.persistence.repository.WorkflowRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of WorkflowService.
 * Handles workflow lifecycle from creation through approval to execution.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private final OrchestratorAgent orchestratorAgent;

    private final WorkflowRepository workflowRepository;

    @Override
    public Optional<Workflow> processEvent(Event event) {

        log.info("Processing event for workflow, eventId: {}, type: {}",
            event.id(), event.eventType());

        try {

            WorkflowPlan plan = orchestratorAgent.planWorkflow(event);

            if (plan == null || plan.nodes().isEmpty()) {

                log.warn("No workflow plan generated, eventId: {}", event.id());

                return Optional.empty();
            }

            Workflow workflow = Workflow.createDraft(
                event.tenantId(),
                event.id(),
                plan.workflowName(),
                plan.description(),
                List.of()
            );

            Workflow submittedWorkflow = workflow.submitForApproval();

            Workflow savedWorkflow = workflowRepository.save(submittedWorkflow);

            log.info("Workflow created and submitted for approval, workflowId: {}, eventId: {}",
                savedWorkflow.id(), event.id());

            return Optional.of(savedWorkflow);

        } catch (Exception e) {

            log.error("Failed to process event, eventId: {}, error: {}",
                event.id(), e.getMessage(), e);

            return Optional.empty();
        }
    }

    @Override
    public Optional<Workflow> getWorkflow(String workflowId) {

        return workflowRepository.findById(workflowId);
    }

    @Override
    public List<Workflow> getWorkflowsAwaitingApproval(String tenantId) {

        return workflowRepository.findByTenantIdAndStatus(tenantId, WorkflowStatus.AWAITING_APPROVAL);
    }

    @Override
    public Workflow approveWorkflow(String workflowId, String approvedBy) {

        log.info("Approving workflow, workflowId: {}, approvedBy: {}", workflowId, approvedBy);

        Workflow workflow = workflowRepository.findById(workflowId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Workflow not found, workflowId: " + workflowId));

        if (workflow.status() != WorkflowStatus.AWAITING_APPROVAL) {

            throw new IllegalStateException(
                "Workflow is not awaiting approval, status: " + workflow.status());
        }

        Workflow approvedWorkflow = workflow.approve(approvedBy);

        Workflow savedWorkflow = workflowRepository.save(approvedWorkflow);

        log.info("Workflow approved, workflowId: {}, status: {}",
            savedWorkflow.id(), savedWorkflow.status());

        return savedWorkflow;
    }

    @Override
    public Workflow rejectWorkflow(String workflowId, String rejectedBy, String reason) {

        log.info("Rejecting workflow, workflowId: {}, rejectedBy: {}, reason: {}",
            workflowId, rejectedBy, reason);

        Workflow workflow = workflowRepository.findById(workflowId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Workflow not found, workflowId: " + workflowId));

        if (workflow.status() != WorkflowStatus.AWAITING_APPROVAL) {

            throw new IllegalStateException(
                "Workflow is not awaiting approval, status: " + workflow.status());
        }

        Workflow rejectedWorkflow = workflow.reject(rejectedBy, reason);

        Workflow savedWorkflow = workflowRepository.save(rejectedWorkflow);

        log.info("Workflow rejected, workflowId: {}, status: {}",
            savedWorkflow.id(), savedWorkflow.status());

        return savedWorkflow;
    }

}
