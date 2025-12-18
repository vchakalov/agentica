package com.agentica.eventsources.common;

/**
 * Status of an event source polling operation.
 */
public enum PollingStatus {

    /**
     * Polling is active and running normally.
     */
    ACTIVE,

    /**
     * Polling is temporarily paused.
     */
    PAUSED,

    /**
     * Polling encountered repeated errors and is stopped.
     */
    ERROR

}
