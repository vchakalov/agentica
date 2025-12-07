package com.agentica.agents.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Configuration properties for Google ADK integration.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "agentica.adk")
public class AdkConfig {

    /**
     * The Gemini API key for accessing Vertex AI.
     */
    private String geminiApiKey;

    /**
     * The default model to use for agents.
     */
    private String defaultModel = "gemini-2.0-flash";

    /**
     * Whether to enable the ADK development UI.
     */
    private boolean devUiEnabled = false;

    /**
     * The port for the ADK development server.
     */
    private int devServerPort = 8081;

}
