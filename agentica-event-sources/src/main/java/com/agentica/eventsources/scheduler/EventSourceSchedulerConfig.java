package com.agentica.eventsources.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration to enable scheduling for event source polling.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(
        prefix = "agentica.event-sources",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class EventSourceSchedulerConfig {

}
