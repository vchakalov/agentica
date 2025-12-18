package com.agentica.api.controller;

import static com.agentica.common.constants.AgenticaConstants.TENANT_ID_HEADER;

import com.agentica.api.dto.response.EventResponse;
import com.agentica.api.dto.response.EventStatsResponse;
import com.agentica.core.domain.Event;
import com.agentica.core.enums.EventStatus;
import com.agentica.core.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Event management endpoints")
public class EventController {

  private final EventService eventService;

  @GetMapping
  @Operation(summary = "List events", description = "Returns all events for the tenant")
  public ResponseEntity<List<EventResponse>> listEvents(
      @RequestHeader(TENANT_ID_HEADER) final String tenantId,
      @RequestParam(required = false) final EventStatus status) {

    log.info("Listing events, tenantId: {}, status: {}", tenantId, status);

    final List<Event> events;

    if (status != null) {
      events = eventService.findByTenantIdAndStatus(tenantId, status);
    } else {
      events = eventService.findByTenantId(tenantId);
    }

    final List<EventResponse> response = events.stream()
        .map(this::toEventResponse)
        .toList();

    return ResponseEntity.ok(response);
  }

  @GetMapping("/{eventId}")
  @Operation(summary = "Get event", description = "Returns a specific event by ID")
  public ResponseEntity<EventResponse> getEvent(
      @RequestHeader(TENANT_ID_HEADER) final String tenantId,
      @PathVariable final String eventId) {

    log.info("Getting event, tenantId: {}, eventId: {}", tenantId, eventId);

    return eventService.findById(eventId)
        .filter(event -> tenantId.equals(event.tenantId()))
        .map(this::toEventResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/stats")
  @Operation(summary = "Get event stats", description = "Returns event statistics for the tenant")
  public ResponseEntity<EventStatsResponse> getEventStats(
      @RequestHeader(TENANT_ID_HEADER) final String tenantId) {

    log.info("Getting event stats, tenantId: {}", tenantId);

    final EventStatsResponse stats = EventStatsResponse.builder()
        .tenantId(tenantId)
        .pendingCount(eventService.countByStatus(tenantId, EventStatus.PENDING))
        .processingCount(eventService.countByStatus(tenantId, EventStatus.PROCESSING))
        .actionableCount(eventService.countByStatus(tenantId, EventStatus.ACTIONABLE))
        .filteredCount(eventService.countByStatus(tenantId, EventStatus.SKIPPED))
        .completedCount(eventService.countByStatus(tenantId, EventStatus.COMPLETED))
        .failedCount(eventService.countByStatus(tenantId, EventStatus.FAILED))
        .build();

    return ResponseEntity.ok(stats);
  }

  @GetMapping("/pending")
  @Operation(summary = "Get pending events", description = "Returns pending events ready for processing")
  public ResponseEntity<List<EventResponse>> getPendingEvents(
      @RequestHeader(TENANT_ID_HEADER) final String tenantId,
      @RequestParam(defaultValue = "50") final int limit) {

    log.info("Getting pending events, tenantId: {}, limit: {}", tenantId, limit);

    final List<Event> events = eventService.getPendingEvents(tenantId, limit);

    final List<EventResponse> response = events.stream()
        .map(this::toEventResponse)
        .toList();

    return ResponseEntity.ok(response);
  }

  private EventResponse toEventResponse(final Event event) {

    return EventResponse.builder()
        .id(event.id())
        .tenantId(event.tenantId())
        .eventType(event.eventType())
        .source(event.source())
        .externalId(event.externalId())
        .status(event.status().name())
        .category(event.category())
        .priority(event.priority())
        .payload(event.payload())
        .receivedAt(event.receivedAt())
        .processedAt(event.processedAt())
        .errorMessage(event.errorMessage())
        .build();
  }

}
