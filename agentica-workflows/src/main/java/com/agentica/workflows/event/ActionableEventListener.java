package com.agentica.workflows.event;

import com.agentica.agents.orchestrator.OrchestratorAgent;
import com.agentica.core.domain.Event;
import com.agentica.core.enums.EventStatus;
import com.agentica.core.service.EventService;
import com.agentica.core.workflow.WorkflowPlan;
import com.agentica.infrastructure.event.ActionableEventPublished;
import com.agentica.workflows.executor.DynamicWorkflowExecutor;
import com.agentica.workflows.executor.ExecutionResult;
import com.agentica.workflows.validator.WorkflowPlanValidator;
import com.agentica.workflows.validator.WorkflowValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActionableEventListener {

  private final OrchestratorAgent orchestratorAgent;

  private final DynamicWorkflowExecutor workflowExecutor;

  private final WorkflowPlanValidator validator;

  private final EventService eventService;

  @Async("eventProcessorExecutor")
  @EventListener
  public void handleActionableEvent(final ActionableEventPublished publishedEvent) {

    final Event event = publishedEvent.getEvent();

    log.info("Processing actionable event, eventId: {}, type: {}, source: {}",
        event.id(), event.eventType(), event.source());

    try {

      final WorkflowPlan plan = orchestratorAgent.planWorkflow(event);

      final ExecutionResult result = workflowExecutor.execute(plan, event);

      handleExecutionResult(event, result);

    } catch (final WorkflowValidationException e) {

      log.error("Workflow validation failed, eventId: {}, planId: {}, errors: {}",
          event.id(), e.getPlanId(), e.getErrors(), e);

      markEventFailed(event, "Workflow validation failed: " + e.getMessage());

    } catch (final Exception e) {

      log.error("Failed to process actionable event, eventId: {}, error: {}",
          event.id(), e.getMessage(), e);

      markEventFailed(event, e.getMessage());
    }
  }

  private void handleExecutionResult(final Event event, final ExecutionResult result) {

    switch (result.status()) {

      case COMPLETED -> {

        eventService.updateStatus(event.id(), EventStatus.COMPLETED, null, null, null);

        log.info("Event completed successfully, eventId: {}, planVersions: {}",
            event.id(), result.planHistory().size());
      }

      case ABORTED -> {

        eventService.updateStatus(event.id(), EventStatus.FAILED, null, null,
            "Workflow aborted: " + result.errorMessage());

        log.warn("Event workflow aborted, eventId: {}, reason: {}",
            event.id(), result.errorMessage());
      }

      case FAILED -> {

        markEventFailed(event, result.errorMessage());

        log.error("Event workflow failed, eventId: {}, error: {}",
            event.id(), result.errorMessage());
      }
    }

    if (result.planHistory() != null && !result.planHistory().isEmpty()) {

      log.debug("Workflow plan history, eventId: {}, versions: {}",
          event.id(), result.planHistory().stream()
              .map(WorkflowPlan::version)
              .toList());
    }
  }

  private void markEventFailed(final Event event, final String errorMessage) {

    try {

      eventService.markAsFailed(event.id(), errorMessage);

    } catch (final Exception updateError) {

      log.error("Failed to mark event as failed, eventId: {}, error: {}",
          event.id(), updateError.getMessage(), updateError);
    }
  }

}
