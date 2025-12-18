package com.agentica.agents.node;

import com.agentica.agents.config.AdkConfig;
import com.agentica.agents.session.AdkSessionManager;
import com.agentica.agents.specialized.facebook.FacebookAgentImpl;
import com.agentica.common.util.JsonUtils;
import com.agentica.core.domain.Event;
import com.agentica.core.state.AgenticaState;
import com.agentica.agents.runner.AgenticaRunner;

import com.google.adk.runner.Runner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Facebook agent node for executing Facebook actions.
 * Uses the FacebookAgentImpl with MCP tools for actual Facebook API operations.
 */
@Slf4j
@Component
public class FacebookAgentNode extends BaseAgentNode {

  private final FacebookAgentImpl facebookAgent;

  public FacebookAgentNode(
      final AdkConfig adkConfig,
      final AdkSessionManager sessionManager,
      final FacebookAgentImpl facebookAgent) {

    super(adkConfig, sessionManager);

    this.facebookAgent = facebookAgent;
  }

  @PostConstruct
  public void init() {

    log.info("Initializing FacebookAgentNode...");

    try {

      this.agent = facebookAgent.getAgent();

      log.info("FacebookAgentNode initialized with MCP tools");

    } catch (final IllegalStateException e) {

      log.warn("FacebookAgent not available, using fallback agent: {}", e.getMessage());
    }
  }

  @Override
  public String getName() {

    return "facebook_agent";
  }

  @Override
  public String getDescription() {

    return "Executes Facebook actions via MCP tools";
  }

  @Override
  public AgentNodeResult execute(final AgenticaState state, final String instruction,
      final Map<String, Object> config) {

    log.info("Executing Facebook agent node");

    try {

      final Event event = state.event()
          .orElseThrow(() -> new IllegalStateException("Event not found in state"));

      final String sessionId = UUID.randomUUID().toString();

      final Session session = sessionManager.createSession(sessionId, event);

      final Runner runner = new AgenticaRunner(
          agent,
          "agentica",
          sessionManager.getSessionService()
      );

      final String prompt = buildPrompt(state, config);

      final Content userMessage = Content.fromParts(Part.fromText(prompt));

      final StringBuilder responseText = new StringBuilder();

      boolean toolExecuted = false;

      for (final com.google.adk.events.Event agentEvent :
          runner.runAsync(event.tenantId(), session.id(), userMessage).blockingIterable()) {

        if (agentEvent.content().isPresent()) {

          final Content content = agentEvent.content().get();

          if (content.parts().isPresent()) {

            for (final Part part : content.parts().get()) {

              if (part.functionCall().isPresent()) {

                toolExecuted = true;

                log.debug("Facebook tool called: {}",
                    part.functionCall().get().name().orElse("unknown"));
              }

              if (part.text().isPresent()) {

                responseText.append(part.text().get());
              }
            }
          }
        }
      }

      final String result = responseText.toString().trim();

      if (!toolExecuted) {

        log.warn("No Facebook tool was executed - may be in fallback mode");

        return AgentNodeResult.escalate(
            "TOOL_NOT_AVAILABLE",
            "Facebook MCP tools not available. Manual action required. Planned action: " + result
        );
      }

      return AgentNodeResult.success(result);

    } catch (final Exception e) {

      log.error("Facebook agent execution failed, error: {}", e.getMessage(), e);

      return AgentNodeResult.fromException(e);
    }
  }

  @Override
  protected String buildPrompt(final AgenticaState state, final Map<String, Object> config) {

    final Event event = state.event()
        .orElseThrow(() -> new IllegalStateException("Event not found in state"));

    final String payloadJson = JsonUtils.toPrettyJson(event.payload())
        .orElse("{}");

    final String nodeOutputsJson = JsonUtils.toPrettyJson(state.nodeOutputs())
        .orElse("{}");

    final Map<String, Object> payload = event.payload();

    final String postId = Optional.ofNullable(payload.get("post_id"))
        .map(Object::toString)
        .orElse("unknown");

    final String commentId = Optional.ofNullable(payload.get("comment_id"))
        .map(Object::toString)
        .orElse("unknown");

    return String.format("""
            Execute the appropriate Facebook action for this request:

            Event Type: %s
            Post ID: %s
            Comment ID: %s

            Full Event Payload:
            %s

            Previous Analysis (from Marketing Agent):
            %s

            Based on the analysis, use the appropriate tool:
            - reply_to_comment: If a response is needed
            - delete_comment: If the comment should be removed
            - get_post_comments: If more context is needed
            """,
        event.eventType(),
        postId,
        commentId,
        payloadJson,
        nodeOutputsJson
    );
  }

}
