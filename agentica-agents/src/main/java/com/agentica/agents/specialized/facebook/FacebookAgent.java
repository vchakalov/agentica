package com.agentica.agents.specialized.facebook;

import com.google.adk.agents.LlmAgent;

/**
 * Agent specialized in Facebook page comment interactions.
 * Provides tools for replying to comments, getting comments, filtering negative comments,
 * and deleting inappropriate comments via the Facebook MCP server.
 */
public interface FacebookAgent {

    /**
     * Returns the underlying ADK LlmAgent configured with Facebook MCP tools.
     *
     * @return the LlmAgent instance
     */
    LlmAgent getAgent();

}
