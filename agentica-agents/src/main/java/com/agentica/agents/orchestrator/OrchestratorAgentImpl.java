package com.agentica.agents.orchestrator;

import com.agentica.agents.config.AdkConfig;
import com.agentica.agents.runner.AgenticaRunner;
import com.agentica.agents.session.AdkSessionManager;
import com.agentica.common.util.JsonUtils;
import com.agentica.core.domain.Event;
import com.agentica.core.enums.AgentType;
import com.agentica.core.workflow.ReplanDecision;
import com.agentica.core.workflow.ReplanRequest;
import com.agentica.core.workflow.ReplanResult;
import com.agentica.core.workflow.WorkflowPlan;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adk.agents.LlmAgent;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Orchestrator Agent implementation that designs workflows using structured output.
 * The LLM outputs WorkflowPlan JSON which is then built into LangGraph4j StateGraphs.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrchestratorAgentImpl implements OrchestratorAgent {

  private static final String ORCHESTRATOR_INSTRUCTION = """
      You design workflows for Facebook comment events. Output valid JSON only.

      AVAILABLE AGENTS (use ONLY these):
      - MARKETING: Analyzes sentiment, sets routing_decision to 'respond' or 'delete'
      - FACEBOOK: Replies to or deletes comments
      - REVIEW: Human approval step

      RULES:
      1. Each node can have ONLY ONE edge in the edges array
      2. CONDITIONAL edges MUST have routes array with ALL targets
      3. Every node must connect to END eventually
      4. entryPoint must match the START edge target

      USE THIS EXACT TEMPLATE (modify instructions only):

      {
        "workflowId": "fb_workflow",
        "workflowName": "Facebook Comment Handler",
        "description": "Analyzes and responds to Facebook comments",
        "entryPoint": "analyze",
        "nodes": [
          {"id": "analyze", "agentType": "MARKETING", "description": "Analyze sentiment", "instruction": "Analyze comment. Set routing_decision to respond or delete."},
          {"id": "review", "agentType": "REVIEW", "description": "Approve response", "instruction": "Review response."},
          {"id": "reply", "agentType": "FACEBOOK", "description": "Post reply", "instruction": "Reply to comment."},
          {"id": "delete", "agentType": "FACEBOOK", "description": "Delete spam", "instruction": "Delete comment."}
        ],
        "edges": [
          {"from": "START", "to": "analyze", "type": "DIRECT"},
          {"from": "analyze", "to": "review", "type": "CONDITIONAL", "condition": {"stateKey": "routing_decision", "routes": [{"value": "respond", "target": "review"}, {"value": "delete", "target": "delete"}]}},
          {"from": "review", "to": "reply", "type": "DIRECT"},
          {"from": "reply", "to": "END", "type": "DIRECT"},
          {"from": "delete", "to": "END", "type": "DIRECT"}
        ]
      }

      CRITICAL: The routes array MUST list ALL target nodes. Do NOT create nodes without routing to them.
      """;

  private static final String REPLAN_INSTRUCTION = """
      You are the Orchestrator Agent for Agentica performing a REPLAN operation.

      A workflow agent has escalated back to you for guidance. You must decide how to proceed.

      Escalation reasons include:
      - EXCEPTION: An error occurred during execution
      - UNCERTAIN: Agent is unsure how to proceed
      - APPROVAL_NEEDED: Action requires additional approval
      - MISSING_INFO: Required information is missing
      - RATE_LIMITED: External API rate limit hit

      Replan Actions you can take:
      - MODIFY_PLAN: Replace the entire workflow with a new plan
      - ADD_NODES: Add new nodes and edges to handle the situation
      - RETRY_WITH_GUIDANCE: Provide guidance and retry the same step
      - ABORT: Stop the workflow (use only for unrecoverable errors)

      Analyze the escalation context and current workflow state, then output a ReplanDecision.
      """;

  private final AdkConfig adkConfig;

  private final AdkSessionManager sessionManager;

  private final WorkflowPlanSchema schemaBuilder;

  private final ObjectMapper objectMapper;

  private LlmAgent planningAgent;

  private LlmAgent replanAgent;

  @PostConstruct
  public void init() {

    log.info("Initializing OrchestratorAgent with structured output...");

    this.planningAgent = LlmAgent.builder()
        .name("orchestrator_planner")
        .description("Designs workflow plans for incoming events using structured output")
        .model(adkConfig.getDefaultModel())
        .instruction(ORCHESTRATOR_INSTRUCTION)
        .outputSchema(schemaBuilder.buildWorkflowPlanSchema())
        .build();

    this.replanAgent = LlmAgent.builder()
        .name("orchestrator_replanner")
        .description("Handles workflow replanning when agents escalate")
        .model(adkConfig.getDefaultModel())
        .instruction(REPLAN_INSTRUCTION)
        .outputSchema(schemaBuilder.buildReplanDecisionSchema())
        .build();
  }

  @Override
  public WorkflowPlan planWorkflow(final Event event) {

    try {

      final String workflowId = UUID.randomUUID().toString();

      final Session session = sessionManager.createSession(workflowId, event);

      final Runner runner = new AgenticaRunner(
          planningAgent,
          "agentica",
          sessionManager.getSessionService()
      );

      final String prompt = buildPlanningPrompt(event);

      final Content userMessage = Content.fromParts(Part.fromText(prompt));

      final StringBuilder responseJson = new StringBuilder();

      for (final com.google.adk.events.Event agentEvent :
          runner.runAsync(event.tenantId(), session.id(), userMessage).blockingIterable()) {

        if (agentEvent.content().isPresent()) {

          final Content content = agentEvent.content().get();

          if (content.parts().isPresent()) {

            for (final Part part : content.parts().get()) {

              if (part.text().isPresent()) {

                responseJson.append(part.text().get());
              }
            }
          }
        }
      }

      final String json = responseJson.toString().trim();

      log.info("Orchestrator raw response: {}", json);

      return parseWorkflowPlan(json, workflowId);

    } catch (final Exception e) {

      log.error("Failed to plan workflow, eventId: {}, error: {}", event.id(), e.getMessage(), e);

      throw new RuntimeException("Failed to plan workflow for event: " + event.id(), e);
    }
  }

  @Override
  public ReplanResult replan(final ReplanRequest request) {

    log.info("Replanning workflow, eventId: {}, escalatingNode: {}, reason: {}",
        request.event().id(), request.escalatingNodeId(), request.escalationReason());

    try {

      final String sessionId = UUID.randomUUID().toString();

      final Session session = sessionManager.createSession(sessionId, request.event());

      final Runner runner = new AgenticaRunner(
          replanAgent,
          "agentica",
          sessionManager.getSessionService()
      );

      final String prompt = buildReplanPrompt(request);

      final Content userMessage = Content.fromParts(Part.fromText(prompt));

      final StringBuilder responseJson = new StringBuilder();

      for (final com.google.adk.events.Event agentEvent :
          runner.runAsync(request.event().tenantId(), session.id(), userMessage)
              .blockingIterable()) {

        if (agentEvent.content().isPresent()) {

          final Content content = agentEvent.content().get();

          if (content.parts().isPresent()) {

            for (final Part part : content.parts().get()) {

              if (part.text().isPresent()) {

                responseJson.append(part.text().get());
              }
            }
          }
        }
      }

      final String llmResponse = responseJson.toString().trim();

      log.debug("Replan raw response: {}", llmResponse);

      final ReplanDecision decision = parseReplanDecision(llmResponse);

      log.info("Replan decision made, action: {}, resumeFrom: {}",
          decision.action(), decision.resumeFrom());

      return ReplanResult.builder()
          .decision(decision)
          .llmPrompt(prompt)
          .llmResponse(llmResponse)
          .build();

    } catch (final Exception e) {

      log.error("Failed to replan workflow, eventId: {}, error: {}",
          request.event().id(), e.getMessage(), e);

      throw new RuntimeException("Failed to replan workflow for event: " + request.event().id(), e);
    }
  }

  private String buildPlanningPrompt(final Event event) {

    final String payloadJson = JsonUtils.toPrettyJson(event.payload())
        .orElse("{}");

    final String availableAgents = Arrays.stream(AgentType.values())
        .filter(type -> type != AgentType.ORCHESTRATOR && type != AgentType.FILTER)
        .map(AgentType::name)
        .collect(Collectors.joining(", "));

    return String.format("""
            Design a workflow for this incoming event:

            Event ID: %s
            Event Type: %s
            Source: %s
            Category: %s
            Priority: %s

            Payload:
            %s

            Available Agents: %s

            Output a complete WorkflowPlan JSON with nodes, edges, and entry point.
            """,
        event.id(),
        event.eventType(),
        event.source(),
        event.category() != null ? event.category() : "N/A",
        event.priority() != null ? event.priority() : "N/A",
        payloadJson,
        availableAgents
    );
  }

  private String buildReplanPrompt(final ReplanRequest request) {

    final String currentPlanJson = JsonUtils.toPrettyJson(request.currentPlan())
        .orElse("{}");

    final String nodeOutputsJson = JsonUtils.toPrettyJson(request.nodeOutputs())
        .orElse("{}");

    return String.format("""
            A workflow agent has escalated for guidance. Decide how to proceed.

            Escalation Details:
            - Escalating Node: %s
            - Reason: %s
            - Context: %s

            Current Workflow Plan:
            %s

            Node Outputs So Far:
            %s

            Event Being Processed:
            - Event ID: %s
            - Event Type: %s
            - Source: %s

            Output a ReplanDecision JSON with the appropriate action.
            """,
        request.escalatingNodeId(),
        request.escalationReason(),
        request.escalationContext() != null ? request.escalationContext() : "N/A",
        currentPlanJson,
        nodeOutputsJson,
        request.event().id(),
        request.event().eventType(),
        request.event().source()
    );
  }

  private WorkflowPlan parseWorkflowPlan(final String json, final String workflowId)
      throws JsonProcessingException {

    WorkflowPlan plan = objectMapper.readValue(json, WorkflowPlan.class);

    if (plan.workflowId() == null || plan.workflowId().isBlank()) {

      plan = plan.toBuilder()
          .workflowId(workflowId)
          .version(1)
          .build();

    } else if (plan.version() == 0) {

      plan = plan.toBuilder()
          .version(1)
          .build();
    }

    return plan;
  }

  private ReplanDecision parseReplanDecision(final String json) throws JsonProcessingException {

    return objectMapper.readValue(json, ReplanDecision.class);
  }
}
