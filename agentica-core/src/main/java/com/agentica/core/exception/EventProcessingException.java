package com.agentica.core.exception;

/**
 * Exception thrown when event processing fails.
 */
public class EventProcessingException extends AgenticaException {

    private final String eventId;

    public EventProcessingException(String eventId, String message) {
        super(message);
        this.eventId = eventId;
    }

    public EventProcessingException(String eventId, String message, Throwable cause) {
        super(message, cause);
        this.eventId = eventId;
    }

    public String getEventId() {
        return eventId;
    }

}
