package com.agentica.api.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Builder;

@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookRequest(

    @NotBlank
    String eventType,

    @NotBlank
    String source,

    String externalId,

    @NotNull
    Map<String, Object> payload

) {

}
