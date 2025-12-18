package com.agentica.eventsources.common;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;

/**
 * Tracks the state of polling for a specific event source.
 * Used to resume polling from the last successful point and handle failures.
 */
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record PollingState(

        String id,

        @NotBlank
        String tenantId,

        @NotBlank
        String sourceType,

        @NotBlank
        String sourceId,

        Instant lastPolledAt,

        String lastCursor,

        int consecutiveFailures,

        Instant nextPollAt,

        PollingStatus status,

        String lastErrorMessage,

        Instant createdAt,

        Instant updatedAt

) {

    /**
     * Creates a new polling state for a source.
     */
    public static PollingState createNew(String tenantId, String sourceType, String sourceId) {

        Instant now = Instant.now();

        return PollingState.builder()
                .tenantId(tenantId)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .consecutiveFailures(0)
                .status(PollingStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Records a successful poll.
     */
    public PollingState markPolled(Instant polledAt) {

        return this.toBuilder()
                .lastPolledAt(polledAt)
                .consecutiveFailures(0)
                .lastErrorMessage(null)
                .status(PollingStatus.ACTIVE)
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * Records a polling failure.
     */
    public PollingState recordFailure(String errorMessage) {

        int failures = this.consecutiveFailures + 1;
        PollingStatus newStatus = failures >= 3 ? PollingStatus.ERROR : this.status;

        return this.toBuilder()
                .consecutiveFailures(failures)
                .lastErrorMessage(errorMessage)
                .status(newStatus)
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * Updates the pagination cursor.
     */
    public PollingState withCursor(String cursor) {

        return this.toBuilder()
                .lastCursor(cursor)
                .updatedAt(Instant.now())
                .build();
    }

}
