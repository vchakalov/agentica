package com.agentica.api.controller;

import com.agentica.api.dto.request.WebhookRequest;
import com.agentica.api.dto.response.WebhookResponse;
import com.agentica.core.domain.Event;
import com.agentica.core.service.EventService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

import static com.agentica.common.constants.AgenticaConstants.TENANT_ID_HEADER;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Webhook ingestion endpoints")
public class WebhookController {

    private final EventService eventService;

    @GetMapping("/hookdeck")
    @Operation(summary = "Receive Hookdeck webhook", description = "Receives normalized webhooks from Hookdeck")
    public ResponseEntity<WebhookResponse> receiveHookdeckWebhook() {

//        log.info("Received Hookdeck webhook, tenantId: {}, eventType: {}, source: {}",
//                tenantId, request.eventType(), request.source());

//        Event event = eventService.ingest(
//                tenantId,
//                request.eventType(),
//                request.source(),
//                request.externalId(),
//                request.payload()
//        );

//        log.info("Event ingested, eventId: {}, tenantId: {}", event.id(), tenantId);

        return ResponseEntity.accepted().body(WebhookResponse.builder()
//                .eventId(event.id())
                .status("ACCEPTED")
                .message("Event received and queued for processing")
                .receivedAt(Instant.now())
                .build());
    }

    @PostMapping("/whatsapp")
    @Operation(summary = "Receive WhatsApp webhook", description = "Receives WhatsApp message webhooks")
    public ResponseEntity<WebhookResponse> receiveWhatsAppWebhook(
            @RequestHeader(TENANT_ID_HEADER) String tenantId,
            @Valid @RequestBody WebhookRequest request) {

        log.info("Received WhatsApp webhook, tenantId: {}, eventType: {}", tenantId, request.eventType());

        Event event = eventService.ingest(
                tenantId,
                request.eventType(),
                "whatsapp",
                request.externalId(),
                request.payload()
        );

        return ResponseEntity.accepted().body(WebhookResponse.builder()
                .eventId(event.id())
                .status("ACCEPTED")
                .message("WhatsApp event received and queued for processing")
                .receivedAt(Instant.now())
                .build());
    }

    @PostMapping("/viber")
    @Operation(summary = "Receive Viber webhook", description = "Receives Viber message webhooks")
    public ResponseEntity<WebhookResponse> receiveViberWebhook(
            @RequestHeader(TENANT_ID_HEADER) String tenantId,
            @Valid @RequestBody WebhookRequest request) {

        log.info("Received Viber webhook, tenantId: {}, eventType: {}", tenantId, request.eventType());

        Event event = eventService.ingest(
                tenantId,
                request.eventType(),
                "viber",
                request.externalId(),
                request.payload()
        );

        return ResponseEntity.accepted().body(WebhookResponse.builder()
                .eventId(event.id())
                .status("ACCEPTED")
                .message("Viber event received and queued for processing")
                .receivedAt(Instant.now())
                .build());
    }

    @PostMapping("/facebook")
    @Operation(summary = "Receive Facebook webhook", description = "Receives Facebook comment and message webhooks")
    public ResponseEntity<WebhookResponse> receiveFacebookWebhook(
            @RequestHeader(TENANT_ID_HEADER) String tenantId,
            @Valid @RequestBody WebhookRequest request) {

        log.info("Received Facebook webhook, tenantId: {}, eventType: {}", tenantId, request.eventType());

        Event event = eventService.ingest(
                tenantId,
                request.eventType(),
                "facebook",
                request.externalId(),
                request.payload()
        );

        return ResponseEntity.accepted().body(WebhookResponse.builder()
                .eventId(event.id())
                .status("ACCEPTED")
                .message("Facebook event received and queued for processing")
                .receivedAt(Instant.now())
                .build());
    }

}
