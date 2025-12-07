package com.agentica.common.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Application-wide constants for Agentica.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AgenticaConstants {

    // API Versioning
    public static final String API_V1 = "/api/v1";

    // Default Values
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // Tenant Header
    public static final String TENANT_ID_HEADER = "X-Tenant-ID";

    // Event Processing
    public static final int EVENT_PROCESSING_BATCH_SIZE = 50;
    public static final long WORKFLOW_POLL_INTERVAL_MS = 30_000L;

    // Agent Types
    public static final String AGENT_TYPE_FILTER = "FILTER";
    public static final String AGENT_TYPE_ORCHESTRATOR = "ORCHESTRATOR";
    public static final String AGENT_TYPE_SUPPORT = "SUPPORT";
    public static final String AGENT_TYPE_FINANCE = "FINANCE";
    public static final String AGENT_TYPE_MARKETING = "MARKETING";

}
