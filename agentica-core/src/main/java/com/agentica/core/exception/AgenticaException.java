package com.agentica.core.exception;

/**
 * Base exception for all Agentica-specific exceptions.
 */
public class AgenticaException extends RuntimeException {

    public AgenticaException(String message) {
        super(message);
    }

    public AgenticaException(String message, Throwable cause) {
        super(message, cause);
    }

}
