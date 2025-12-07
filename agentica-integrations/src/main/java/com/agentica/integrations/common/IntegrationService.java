package com.agentica.integrations.common;

import java.util.Map;

/**
 * Base interface for all external integrations.
 */
public interface IntegrationService {

    /**
     * Returns the name of this integration.
     */
    String getName();

    /**
     * Checks if the integration is properly configured and available.
     */
    boolean isAvailable();

    /**
     * Executes an action through this integration.
     *
     * @param action the action to execute
     * @param parameters the action parameters
     * @return the result of the action
     */
    IntegrationResult execute(String action, Map<String, Object> parameters);

    /**
     * Result of an integration action.
     */
    record IntegrationResult(

            boolean success,

            String message,

            Map<String, Object> data

    ) {

        /**
         * Creates a successful result.
         */
        public static IntegrationResult success(String message, Map<String, Object> data) {
            return new IntegrationResult(true, message, data);
        }

        /**
         * Creates a successful result with just a message.
         */
        public static IntegrationResult success(String message) {
            return new IntegrationResult(true, message, Map.of());
        }

        /**
         * Creates a failure result.
         */
        public static IntegrationResult failure(String message) {
            return new IntegrationResult(false, message, Map.of());
        }

    }

}
