package com.agentica.core.domain;

import com.agentica.core.enums.EventStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.Map;

/**
 * Represents an incoming event from external sources.
 * Events are the primary input to the Agentica system.
 */
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Event(

        String id,

        @NotBlank
        String tenantId,

        @NotBlank
        String eventType,

        @NotBlank
        String source,

        @NotNull
        Map<String, Object> payload,

        @NotNull
        EventStatus status,

        String externalId,

        Instant receivedAt,

        Instant processedAt,

        String errorMessage

) {

    /**
     * Creates a new pending event.
     */
    public static Event createPending(String tenantId, String eventType, String source, Map<String, Object> payload) {
        return Event.builder()
                .tenantId(tenantId)
                .eventType(eventType)
                .source(source)
                .payload(payload)
                .status(EventStatus.PENDING)
                .receivedAt(Instant.now())
                .build();
    }

    /**
     * Returns a copy of this event with status updated to PROCESSING.
     */
    public Event markAsProcessing() {
        return this.toBuilder()
                .status(EventStatus.PROCESSING)
                .build();
    }

    /**
     * Returns a copy of this event with status updated to ACTIONABLE.
     */
    public Event markAsActionable() {
        return this.toBuilder()
                .status(EventStatus.ACTIONABLE)
                .processedAt(Instant.now())
                .build();
    }

    /**
     * Returns a copy of this event with status updated to FILTERED.
     */
    public Event markAsFiltered() {
        return this.toBuilder()
                .status(EventStatus.FILTERED)
                .processedAt(Instant.now())
                .build();
    }

    /**
     * Returns a copy of this event with status updated to FAILED.
     */
    public Event markAsFailed(String errorMessage) {
        return this.toBuilder()
                .status(EventStatus.FAILED)
                .processedAt(Instant.now())
                .errorMessage(errorMessage)
                .build();
    }

}
