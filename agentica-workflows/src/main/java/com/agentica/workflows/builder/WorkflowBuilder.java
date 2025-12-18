package com.agentica.workflows.builder;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

import com.agentica.agents.node.AgentNode;
import com.agentica.agents.node.AgentNodeResult;
import com.agentica.agents.orchestrator.OrchestratorAgent;
import com.agentica.agents.registry.AgentNodeRegistry;
import com.agentica.core.state.AgenticaState;
import com.agentica.core.workflow.ConditionalConfig;
import com.agentica.core.workflow.EdgeType;
import com.agentica.core.workflow.ReplanRequest;
import com.agentica.core.workflow.ReplanResult;
import com.agentica.core.workflow.WorkflowEdge;
import com.agentica.core.workflow.WorkflowNode;
import com.agentica.core.workflow.WorkflowPlan;
import com.agentica.workflows.registry.WorkflowRegistry;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowBuilder {

  private static final String REPLAN_NODE_ID = "orchestrator_replan";

  private final AgentNodeRegistry agentRegistry;

  private final OrchestratorAgent orchestratorAgent;

  private final WorkflowRegistry workflowRegistry;

  /**
   * Builds a compiled LangGraph4j graph from a WorkflowPlan.
   *
   * @param plan the workflow plan to build
   * @param eventId the event ID for tracking and visualization
   * @return the compiled graph ready for execution
   */
  public CompiledGraph<AgenticaState> build(final WorkflowPlan plan, final String eventId) {

    log.info("Building workflow graph, planId: {}, name: {}, nodes: {}, edges: {}",
        plan.workflowId(), plan.workflowName(), plan.nodes().size(), plan.edges().size());

    try {

      final StateGraph<AgenticaState> graph = new StateGraph<>(
          AgenticaState.SCHEMA,
          AgenticaState::new
      );

      graph.addNode(REPLAN_NODE_ID, createReplanNode());
      graph.addEdge(REPLAN_NODE_ID, END);

      log.debug("Added replan node with edge to END");

      for (final WorkflowNode node : plan.nodes()) {

        graph.addNode(node.id(), createAgentNode(node));

        log.debug("Added node, id: {}, agentType: {}", node.id(), node.agentType());
      }

      for (final WorkflowEdge edge : plan.edges()) {

        addEdgeWithEscalation(graph, edge);
      }

      workflowRegistry.updateLatest(eventId, plan, graph);

      final CompiledGraph<AgenticaState> compiledGraph = graph.compile();

      log.info("Workflow graph built successfully, planId: {}", plan.workflowId());

      return compiledGraph;

    } catch (final Exception e) {

      log.error("Failed to build workflow graph, planId: {}, error: {}",
          plan.workflowId(), e.getMessage(), e);

      throw new RuntimeException("Failed to build workflow graph: " + plan.workflowId(), e);
    }
  }

  private AsyncNodeAction<AgenticaState> createAgentNode(final WorkflowNode node) {

    return node_async(state -> {

      log.debug("Executing agent node, id: {}, agentType: {}", node.id(), node.agentType());

      final AgentNode agentNode = agentRegistry.get(node.agentType());

      final AgentNodeResult result = agentNode.execute(state, node.instruction(), node.config());

      final Map<String, Object> updates = new HashMap<>();

      updates.put(AgenticaState.KEY_CURRENT_NODE_ID, node.id());

      if (result.needsEscalation()) {

        updates.put(AgenticaState.KEY_NEEDS_REPLAN, true);
        updates.put(AgenticaState.KEY_ESCALATION_REASON, result.escalationReason());
        updates.put(AgenticaState.KEY_ESCALATION_CONTEXT, result.escalationContext());
        updates.put(AgenticaState.KEY_RESUME_FROM_NODE, node.id());

        log.info("Agent node escalating, id: {}, reason: {}",
            node.id(), result.escalationReason());

      } else {

        updates.put(AgenticaState.KEY_NEEDS_REPLAN, false);

        final Map<String, Object> nodeOutputs = new HashMap<>(state.nodeOutputs());

        nodeOutputs.put(node.id(), result.output());

        updates.put(AgenticaState.KEY_NODE_OUTPUTS, nodeOutputs);

        log.debug("Agent node completed, id: {}", node.id());
      }

      return updates;
    });
  }

  private AsyncNodeAction<AgenticaState> createReplanNode() {

    return node_async(state -> {

      final ReplanRequest request = ReplanRequest.builder()
          .event(state.event().orElseThrow())
          .currentPlan(state.currentPlan().orElseThrow())
          .escalatingNodeId(state.resumeFromNode().orElse("unknown"))
          .escalationReason(state.escalationReason().orElse("unknown"))
          .escalationContext(state.escalationContext().orElse(null))
          .currentState(state.data())
          .nodeOutputs(state.nodeOutputs())
          .build();

      final ReplanResult replanResult = orchestratorAgent.replan(request);

      final Map<String, Object> updates = new HashMap<>();

      updates.put(AgenticaState.KEY_REPLAN_DECISION, replanResult.decision());
      updates.put(AgenticaState.KEY_REPLAN_LLM_PROMPT, replanResult.llmPrompt());
      updates.put(AgenticaState.KEY_REPLAN_LLM_RESPONSE, replanResult.llmResponse());

      return updates;
    });
  }

  /**
   * Adds an edge with integrated escalation routing.
   * All edges from agent nodes include escalation check to replan node.
   */
  private void addEdgeWithEscalation(final StateGraph<AgenticaState> graph, final WorkflowEdge edge)
      throws GraphStateException {

    final String from = normalizeNodeId(edge.from());
    final String to = normalizeNodeId(edge.to());

    if (START.equals(from)) {

      graph.addEdge(from, to);

      log.debug("Added start edge, from: {}, to: {}", from, to);

      return;
    }

    if (edge.type() == EdgeType.DIRECT) {

      graph.addConditionalEdges(
          from,
          edge_async(state -> {

            if (state.needsReplan()) {

              return REPLAN_NODE_ID;
            }

            return "continue";
          }),
          Map.of(
              REPLAN_NODE_ID, REPLAN_NODE_ID,
              "continue", to
          )
      );

    } else if (edge.type() == EdgeType.CONDITIONAL && edge.condition() != null) {

      final ConditionalConfig condition = edge.condition();

      final Map<String, String> routeMapping = new HashMap<>();

      routeMapping.put(REPLAN_NODE_ID, REPLAN_NODE_ID);

      final Map<String, String> routes = condition.routesAsMap();

      for (final Map.Entry<String, String> route : routes.entrySet()) {

        routeMapping.put(route.getKey(), normalizeNodeId(route.getValue()));
      }

      routeMapping.put("default", to);

      graph.addConditionalEdges(
          from,
          edge_async(state -> {

            if (state.needsReplan()) {

              return REPLAN_NODE_ID;
            }

            final String stateKey = condition.stateKey();

            if (stateKey != null && !stateKey.isBlank()) {

              final Object value = state.nodeOutputs().get(stateKey);

              if (value != null) {

                final String key = value.toString();

                if (routeMapping.containsKey(key)) {

                  return key;
                }
              }
            }

            return "default";
          }),
          routeMapping
      );

      log.debug("Added conditional edge with escalation, from: {}, routes: {}", from,
          routeMapping.keySet());
    }
  }

  private String findNextNode(final String nodeId, final WorkflowPlan plan) {

    for (final WorkflowEdge edge : plan.edges()) {

      if (edge.from().equals(nodeId)) {

        return normalizeNodeId(edge.to());
      }
    }

    return END;
  }

  private String normalizeNodeId(final String nodeId) {

    if ("START".equalsIgnoreCase(nodeId)) {

      return START;
    }

    if ("END".equalsIgnoreCase(nodeId)) {

      return END;
    }

    return nodeId;
  }

}
