package com.agentica.api.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

import java.time.Instant;
import java.util.Map;

@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventResponse(

    String id,

    String tenantId,

    String eventType,

    String source,

    String externalId,

    String status,

    String category,

    Integer priority,

    Map<String, Object> payload,

    Instant receivedAt,

    Instant processedAt,

    String errorMessage

) {

}
