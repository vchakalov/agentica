package com.agentica.api.controller;

import com.agentica.api.dto.request.WebhookRequest;
import com.agentica.api.dto.response.WebhookResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

import static com.agentica.common.constants.AgenticaConstants.TENANT_ID_HEADER;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Webhook ingestion endpoints")
public class WebhookController {

    @PostMapping("/hookdeck")
    @Operation(summary = "Receive Hookdeck webhook", description = "Receives normalized webhooks from Hookdeck")
    public ResponseEntity<WebhookResponse> receiveHookdeckWebhook(
            @RequestHeader(TENANT_ID_HEADER) String tenantId,
            @Valid @RequestBody WebhookRequest request) {

        log.info("Received webhook for tenant: {}, type: {}, source: {}",
                tenantId, request.eventType(), request.source());

        // TODO: Implement actual event processing
        // EventService.ingest(tenantId, request)

        String eventId = UUID.randomUUID().toString();

        log.info("Created event: {} for tenant: {}", eventId, tenantId);

        return ResponseEntity.accepted().body(WebhookResponse.builder()
                .eventId(eventId)
                .status("ACCEPTED")
                .message("Event received and queued for processing")
                .receivedAt(Instant.now())
                .build());
    }

}
