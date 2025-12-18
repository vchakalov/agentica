package com.agentica.workflows.executor;

import com.agentica.core.domain.Event;
import com.agentica.core.event.workflow.NodeCompletedEvent;
import com.agentica.core.event.workflow.NodeEscalatedEvent;
import com.agentica.core.event.workflow.NodeStartedEvent;
import com.agentica.core.event.workflow.WorkflowCompletedEvent;
import com.agentica.core.event.workflow.WorkflowReplanEvent;
import com.agentica.core.event.workflow.WorkflowStartedEvent;
import com.agentica.core.state.AgenticaState;
import com.agentica.core.workflow.ReplanAction;
import com.agentica.core.workflow.ReplanDecision;
import com.agentica.core.workflow.WorkflowEdge;
import com.agentica.core.workflow.WorkflowNode;
import com.agentica.core.workflow.WorkflowPlan;
import com.agentica.workflows.builder.WorkflowBuilder;
import com.agentica.workflows.validator.WorkflowPlanValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.NodeOutput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicWorkflowExecutor {

  private final WorkflowBuilder workflowBuilder;

  private final WorkflowPlanValidator validator;

  private final ApplicationEventPublisher eventPublisher;

  @Value("${agentica.workflow.max-replan-iterations:5}")
  private int maxReplanIterations;

  public ExecutionResult execute(final WorkflowPlan initialPlan, final Event event) {

    final long executionStartTime = System.currentTimeMillis();

    log.info("Starting workflow execution, planId: {}, eventId: {}",
        initialPlan.workflowId(), event.id());

    validator.validate(initialPlan);

    eventPublisher.publishEvent(WorkflowStartedEvent.of(event.id(), initialPlan, event));

    Map<String, WorkflowNode> nodeMap;

    WorkflowPlan currentPlan = initialPlan;

    AgenticaState state = AgenticaState.forEvent(event, initialPlan);

    final List<WorkflowPlan> planHistory = new ArrayList<>();

    planHistory.add(initialPlan);

    int replanCount = 0;

    try {

      while (replanCount <= maxReplanIterations) {

        final CompiledGraph<AgenticaState> graph = workflowBuilder.build(currentPlan, event.id());

        nodeMap = buildNodeMap(currentPlan);

        boolean needsRebuild = false;

        String previousNode = null;

        long nodeStartTime = System.currentTimeMillis();

        for (final NodeOutput<AgenticaState> nodeOutput : graph.stream(state.data())) {

          state = nodeOutput.state();

          final String currentNode = nodeOutput.node();

          if (previousNode != null && !previousNode.equals(currentNode)) {

            publishNodeCompletion(event.id(), previousNode, nodeMap, nodeStartTime, state);

            nodeStartTime = System.currentTimeMillis();
          }

          if (!isSpecialNode(currentNode) && !currentNode.equals(previousNode)) {

            publishNodeStart(event.id(), currentNode, nodeMap);
          }

          if (state.hasReplanDecision()) {

            final ReplanDecision decision = state.replanDecision().get();

            if (previousNode != null) {

              publishNodeEscalation(
                  event.id(),
                  previousNode,
                  nodeMap,
                  state.escalationReason().orElse("unknown"),
                  state.escalationContext().orElse(null),
                  nodeStartTime
              );
            }

            if (decision.action() == ReplanAction.ABORT) {

              log.warn("Workflow aborted, reason: {}", decision.reason());

              final long totalDurationMs = System.currentTimeMillis() - executionStartTime;

              eventPublisher.publishEvent(
                  WorkflowCompletedEvent.aborted(
                      event.id(),
                      decision.reason(),
                      totalDurationMs,
                      replanCount
                  )
              );

              return ExecutionResult.aborted(decision.reason(), planHistory);
            }

            final WorkflowPlan previousPlan = currentPlan;

            currentPlan = applyReplan(currentPlan, decision);

            planHistory.add(currentPlan);

            replanCount++;

            eventPublisher.publishEvent(WorkflowReplanEvent.of(
                event.id(),
                previousPlan,
                currentPlan,
                decision,
                previousNode,
                state.escalationReason().orElse("unknown"),
                state.escalationContext().orElse(null),
                replanCount,
                state.replanLlmPrompt().orElse(null),
                state.replanLlmResponse().orElse(null)
            ));

            state = clearReplanState(state, currentPlan);

            needsRebuild = true;

            break;
          }

          if ("__end__".equals(currentNode)) {

            if (previousNode != null && !isSpecialNode(previousNode)) {

              publishNodeCompletion(event.id(), previousNode, nodeMap, nodeStartTime, state);
            }

            final long totalDurationMs = System.currentTimeMillis() - executionStartTime;

            eventPublisher.publishEvent(
                WorkflowCompletedEvent.completed(event.id(), totalDurationMs, replanCount)
            );

            return ExecutionResult.completed(state, planHistory);
          }

          previousNode = currentNode;
        }

        if (!needsRebuild) {

          if (previousNode != null && !isSpecialNode(previousNode)) {

            publishNodeCompletion(event.id(), previousNode, nodeMap, nodeStartTime, state);
          }

          final long totalDurationMs = System.currentTimeMillis() - executionStartTime;

          eventPublisher.publishEvent(
              WorkflowCompletedEvent.completed(event.id(), totalDurationMs, replanCount)
          );

          return ExecutionResult.completed(state, planHistory);
        }
      }

      final long totalDurationMs = System.currentTimeMillis() - executionStartTime;

      final String errorMessage = "Max replan iterations (" + maxReplanIterations + ") exceeded";

      eventPublisher.publishEvent(
          WorkflowCompletedEvent.failed(event.id(), errorMessage, totalDurationMs, replanCount)
      );

      return ExecutionResult.failed(errorMessage, planHistory);

    } catch (final Exception e) {

      log.error("Workflow execution failed, eventId: {}, error: {}",
          event.id(), e.getMessage(), e);

      final long totalDurationMs = System.currentTimeMillis() - executionStartTime;

      eventPublisher.publishEvent(
          WorkflowCompletedEvent.failed(event.id(), e.getMessage(), totalDurationMs, replanCount)
      );

      throw e;
    }
  }

  private Map<String, WorkflowNode> buildNodeMap(final WorkflowPlan plan) {

    return plan.nodes().stream()
        .collect(Collectors.toMap(WorkflowNode::id, Function.identity()));
  }

  private void publishNodeStart(final String eventId, final String nodeId,
      final Map<String, WorkflowNode> nodeMap) {

    final WorkflowNode node = nodeMap.get(nodeId);

    if (node != null) {

      eventPublisher.publishEvent(NodeStartedEvent.of(
          eventId,
          nodeId,
          node.agentType(),
          node.instruction(),
          node.config()
      ));
    }
  }

  private void publishNodeCompletion(final String eventId, final String nodeId,
      final Map<String, WorkflowNode> nodeMap, final long startTime, final AgenticaState state) {

    if (isSpecialNode(nodeId)) {

      return;
    }

    final long durationMs = System.currentTimeMillis() - startTime;

    final WorkflowNode node = nodeMap.get(nodeId);

    final Object output = state.nodeOutputs().get(nodeId);

    if (node != null) {

      eventPublisher.publishEvent(NodeCompletedEvent.of(
          eventId,
          nodeId,
          node.agentType(),
          output,
          durationMs
      ));
    }
  }

  private void publishNodeEscalation(final String eventId, final String nodeId,
      final Map<String, WorkflowNode> nodeMap, final String reason, final String context,
      final long startTime) {

    if (isSpecialNode(nodeId)) {

      return;
    }

    final long durationMs = System.currentTimeMillis() - startTime;

    final WorkflowNode node = nodeMap.get(nodeId);

    if (node != null) {

      eventPublisher.publishEvent(NodeEscalatedEvent.of(
          eventId,
          nodeId,
          node.agentType(),
          reason,
          context,
          durationMs
      ));
    }
  }

  private boolean isSpecialNode(final String nodeId) {

    return nodeId == null
        || "__start__".equals(nodeId)
        || "__end__".equals(nodeId)
        || "orchestrator_replan".equals(nodeId);
  }

  private WorkflowPlan applyReplan(final WorkflowPlan current, final ReplanDecision decision) {

    return switch (decision.action()) {

      case MODIFY_PLAN -> {

        if (decision.modifiedPlan() == null) {

          throw new IllegalStateException("MODIFY_PLAN requires modifiedPlan");
        }

        yield WorkflowPlan.withVersion(
            decision.modifiedPlan(),
            current.version() + 1
        );
      }

      case ADD_NODES -> {

        final List<WorkflowNode> allNodes = new ArrayList<>(current.nodes());

        if (decision.newNodes() != null) {

          allNodes.addAll(decision.newNodes());
        }

        final List<WorkflowEdge> allEdges = new ArrayList<>(current.edges());

        if (decision.newEdges() != null) {

          allEdges.addAll(decision.newEdges());
        }

        yield current.toBuilder()
            .nodes(allNodes)
            .edges(allEdges)
            .version(current.version() + 1)
            .build();
      }

      case RETRY_WITH_GUIDANCE -> current;

      case ABORT -> throw new WorkflowAbortedException(decision.reason());
    };
  }

  private AgenticaState clearReplanState(final AgenticaState state, final WorkflowPlan newPlan) {

    final var data = new java.util.HashMap<>(state.data());

    data.put(AgenticaState.KEY_NEEDS_REPLAN, false);
    data.put(AgenticaState.KEY_ESCALATION_REASON, null);
    data.put(AgenticaState.KEY_ESCALATION_CONTEXT, null);
    data.put(AgenticaState.KEY_RESUME_FROM_NODE, null);
    data.put(AgenticaState.KEY_REPLAN_DECISION, null);
    data.put(AgenticaState.KEY_CURRENT_PLAN, newPlan);
    data.put(AgenticaState.KEY_PLAN_VERSION, newPlan.version());

    return new AgenticaState(data);
  }

}
