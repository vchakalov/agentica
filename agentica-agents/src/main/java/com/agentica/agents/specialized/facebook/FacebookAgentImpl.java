package com.agentica.agents.specialized.facebook;

import com.agentica.agents.config.AdkConfig;
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.mcp.McpToolset;
import com.google.adk.tools.mcp.StdioServerParameters;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Facebook Agent implementation using McpToolset for Facebook MCP server integration.
 * Specialized in comment management operations for MVP.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FacebookAgentImpl implements FacebookAgent {

  private static final String FACEBOOK_INSTRUCTION = """
      You are a Facebook Agent specialized in managing Facebook page comment interactions.

      Your capabilities (comment-focused for MVP):
      - reply_to_comment: Reply to comments professionally
      - get_post_comments: Retrieve comments on a post
      - filter_negative_comments: Identify negative/problematic comments
      - delete_comment: Remove inappropriate comments

      Guidelines:
      - Be professional and courteous in all replies
      - For questions: provide helpful, accurate information
      - For complaints: acknowledge the issue, apologize, offer solutions
      - For positive feedback: thank the customer sincerely
      - For spam/inappropriate: recommend deletion

      When replying to comments:
      1. Analyze the sentiment and intent of the comment
      2. Craft an appropriate response based on the category
      3. Use the reply_to_comment tool with the correct post_id, comment_id, and message

      Always respond in a way that reflects positively on the brand.
      """;

  private static final List<String> COMMENT_TOOLS = List.of(
      "reply_to_comment",
      "get_post_comments",
      "filter_negative_comments",
      "delete_comment"
  );

  private final AdkConfig adkConfig;

  @Value("${agentica.mcp.facebook.server-path:}")
  private String facebookMcpServerPath;

  @Value("${agentica.mcp.facebook.timeout-seconds:30}")
  private int timeoutSeconds;

  @Value("${agentica.mcp.facebook.page-access-token:}")
  private String pageAccessToken;

  @Value("${agentica.mcp.facebook.page-id:}")
  private String pageId;

  private LlmAgent agent;

  private McpToolset mcpToolset;

  @PostConstruct
  public void init() {

    if (facebookMcpServerPath == null || facebookMcpServerPath.isBlank()) {

      log.warn("Facebook MCP server path not configured, skipping FacebookAgent initialization");

      return;
    }

    log.info("Initializing FacebookAgent with MCP server, path: {}", facebookMcpServerPath);

    try {

      initializeMcpToolset();

    } catch (final Exception e) {

      log.error("Failed to initialize FacebookAgent, error: {}", e.getMessage(), e);

      throw new RuntimeException("Failed to initialize FacebookAgent", e);
    }
  }

  private void initializeMcpToolset() {

    final StdioServerParameters stdioParams = StdioServerParameters.builder()
        .command("uv")
        .args(List.of(
            "--directory",
            facebookMcpServerPath,
            "run",
            "facebook-mcp-server"
        ))
        .env(Map.of(
            "FACEBOOK_PAGE_ACCESS_TOKEN", pageAccessToken,
            "FACEBOOK_PAGE_ID", pageId
        ))
        .build();

    log.info("Connecting to Facebook MCP server via stdio...");

    this.mcpToolset = new McpToolset(
        stdioParams.toServerParameters(),
        Optional.of(COMMENT_TOOLS)
    );

    log.info("McpToolset created with tool filter: {}", COMMENT_TOOLS);

    this.agent = LlmAgent.builder()
        .name("facebook_agent")
        .description("Handles Facebook comment interactions: reply, get, filter, delete")
        .model(adkConfig.getDefaultModel())
        .instruction(FACEBOOK_INSTRUCTION)
        .tools(List.of(mcpToolset))
        .build();

    log.info("FacebookAgent initialized successfully with MCP toolset");
  }

  @PreDestroy
  public void cleanup() {

    if (mcpToolset != null) {

      try {

        mcpToolset.close();

        log.info("Facebook MCP connection closed");

      } catch (final Exception e) {

        log.error("Error closing MCP connection, error: {}", e.getMessage(), e);
      }
    }
  }

  @Override
  public LlmAgent getAgent() {

    if (agent == null) {

      throw new IllegalStateException(
          "FacebookAgent not initialized. Check MCP server configuration.");
    }

    return agent;
  }

}
