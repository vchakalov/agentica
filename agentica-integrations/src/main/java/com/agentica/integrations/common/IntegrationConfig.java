package com.agentica.integrations.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Configuration properties for external integrations.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "agentica.integrations")
public class IntegrationConfig {

    /**
     * Default timeout for integration calls in milliseconds.
     */
    private long defaultTimeoutMs = 30_000L;

    /**
     * Number of retry attempts for failed integration calls.
     */
    private int retryAttempts = 3;

    /**
     * Delay between retry attempts in milliseconds.
     */
    private long retryDelayMs = 1_000L;

}
