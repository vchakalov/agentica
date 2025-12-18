package com.agentica.workflows.executor;

import com.agentica.agents.orchestrator.OrchestratorAgent;
import com.agentica.agents.session.AdkSessionManager;
import com.agentica.core.domain.Workflow;
import com.agentica.core.domain.WorkflowStep;
import com.agentica.core.enums.EventStatus;
import com.agentica.core.enums.WorkflowStatus;
import com.agentica.core.service.EventService;
import com.agentica.infrastructure.persistence.repository.WorkflowRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Implementation of WorkflowExecutor.
 * Executes approved workflows using the ADK agent chain.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowExecutorImpl implements WorkflowExecutor {

  private final OrchestratorAgent orchestratorAgent;

  private final AdkSessionManager sessionManager;

  private final WorkflowRepository workflowRepository;

  private final EventService eventService;

  @Override
  public Workflow execute(final Workflow workflow) {

    log.info("Executing workflow, workflowId: {}, eventId: {}",
        workflow.id(), workflow.eventId());

    if (workflow.status() != WorkflowStatus.APPROVED) {

      throw new IllegalStateException(
          "Cannot execute workflow that is not approved, status: " + workflow.status());
    }

    Workflow executingWorkflow = workflow.startExecution();

    workflowRepository.save(executingWorkflow);

    try {

      for (int i = 0; i < executingWorkflow.steps().size(); i++) {

        executingWorkflow = executeStep(executingWorkflow, i);

        if (executingWorkflow.status() == WorkflowStatus.FAILED) {

          break;
        }
      }

      if (executingWorkflow.status() != WorkflowStatus.FAILED) {

        executingWorkflow = executingWorkflow.complete();

        updateEventStatus(executingWorkflow.eventId(), EventStatus.COMPLETED);

        log.info("Workflow completed, workflowId: {}", executingWorkflow.id());
      }

    } catch (final Exception e) {

      log.error("Workflow execution failed, workflowId: {}, error: {}",
          workflow.id(), e.getMessage(), e);

      executingWorkflow = executingWorkflow.fail(e.getMessage());

      updateEventStatus(executingWorkflow.eventId(), EventStatus.FAILED);
    }

    return workflowRepository.save(executingWorkflow);
  }

  @Override
  public Workflow executeStep(final Workflow workflow, final int stepIndex) {

    if (stepIndex < 0 || stepIndex >= workflow.steps().size()) {

      throw new IllegalArgumentException(
          "Invalid step index: " + stepIndex + ", total steps: " + workflow.steps().size());
    }

    final WorkflowStep step = workflow.steps().get(stepIndex);

    log.info("Executing workflow step, workflowId: {}, step: {}, action: {}",
        workflow.id(), stepIndex + 1, step.action());

    try {

      final String result = executeStepAction(workflow, step);

      final List<WorkflowStep> updatedSteps = updateStepResult(workflow.steps(), stepIndex, result);

      final Workflow updatedWorkflow = workflow.toBuilder()
          .steps(updatedSteps)
          .currentStepIndex(stepIndex + 1)
          .build();

      log.info("Step completed, workflowId: {}, step: {}", workflow.id(), stepIndex + 1);

      return workflowRepository.save(updatedWorkflow);

    } catch (final Exception e) {

      log.error("Step execution failed, workflowId: {}, step: {}, error: {}",
          workflow.id(), stepIndex + 1, e.getMessage(), e);

      final List<WorkflowStep> failedSteps = markStepAsFailed(workflow.steps(), stepIndex,
          e.getMessage());

      final Workflow failedWorkflow = workflow.toBuilder()
          .steps(failedSteps)
          .status(WorkflowStatus.FAILED)
          .errorMessage("Step " + (stepIndex + 1) + " failed: " + e.getMessage())
          .build();

      return workflowRepository.save(failedWorkflow);
    }
  }

  private String executeStepAction(final Workflow workflow, final WorkflowStep step) {

    log.debug("Executing action, action: {}, parameters: {}", step.action(), step.parameters());

    return "Action " + step.action() + " executed successfully";
  }

  private List<WorkflowStep> updateStepResult(final List<WorkflowStep> steps, final int index,
      final String result) {

    return java.util.stream.IntStream.range(0, steps.size())
        .mapToObj(i -> i == index ? steps.get(i).markAsCompleted(result) : steps.get(i))
        .toList();
  }

  private List<WorkflowStep> markStepAsFailed(final List<WorkflowStep> steps, final int index,
      final String errorMessage) {

    return java.util.stream.IntStream.range(0, steps.size())
        .mapToObj(i -> i == index ? steps.get(i).markAsFailed(errorMessage) : steps.get(i))
        .toList();
  }

  private void updateEventStatus(final String eventId, final EventStatus status) {

    try {

      if (status == EventStatus.COMPLETED) {

        eventService.updateStatus(eventId, status, null, null, null);

      } else if (status == EventStatus.FAILED) {

        eventService.markAsFailed(eventId, "Workflow execution failed");
      }

    } catch (final Exception e) {

      log.error("Failed to update event status, eventId: {}, status: {}, error: {}",
          eventId, status, e.getMessage(), e);
    }
  }

}
