package com.agentica.eventsources.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Global configuration for event sources.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "agentica.event-sources")
public class EventSourceConfig {

    private boolean enabled = true;

    private int defaultPollIntervalMs = 60_000;

    private int maxRetryAttempts = 3;

    private long retryBackoffMs = 5_000;

}
