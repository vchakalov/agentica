package com.agentica.eventsources.facebook.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Configuration for Facebook Graph API integration.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "agentica.event-sources.facebook")
public class FacebookConfig {

    private boolean enabled = true;

    private String graphApiVersion = "v21.0";

    private String graphApiBaseUrl = "https://graph.facebook.com";

    private long pollIntervalMs = 60_000;

    private int maxCommentsPerPoll = 500;

    private int initialLookbackHours = 24;

    private int connectionTimeoutMs = 10_000;

    private int readTimeoutMs = 30_000;

}
