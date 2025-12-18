package com.agentica.eventsources.common;

/**
 * Exception thrown when an event source operation fails.
 */
public class EventSourceException extends RuntimeException {

    public EventSourceException(String message) {
        super(message);
    }

    public EventSourceException(String message, Throwable cause) {
        super(message, cause);
    }

}
