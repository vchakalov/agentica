package com.agentica.core.enums;

/**
 * Types of AI agents in the system.
 */
public enum AgentType {

    /**
     * Filters incoming events to determine if they require action.
     */
    FILTER,

    /**
     * Orchestrates workflow creation and task delegation.
     */
    ORCHESTRATOR,

    /**
     * Handles customer support tasks.
     */
    SUPPORT,

    /**
     * Handles financial operations.
     */
    FINANCE,

    /**
     * Handles marketing activities.
     */
    MARKETING

}
