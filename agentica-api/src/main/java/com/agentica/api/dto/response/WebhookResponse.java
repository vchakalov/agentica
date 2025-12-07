package com.agentica.api.dto.response;

import lombok.Builder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

/**
 * Response DTO for webhook ingestion.
 */
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookResponse(

        String eventId,

        String status,

        String message,

        Instant receivedAt

) {}
