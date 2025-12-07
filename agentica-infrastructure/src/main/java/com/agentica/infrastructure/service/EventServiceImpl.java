package com.agentica.infrastructure.service;

import com.agentica.core.domain.Event;
import com.agentica.core.enums.EventStatus;
import com.agentica.core.service.EventService;
import com.agentica.infrastructure.persistence.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of EventService.
 * Handles event ingestion and lifecycle management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    public Event ingest(String tenantId, String eventType, String source, String externalId, Map<String, Object> payload) {

        log.info("Ingesting event, tenantId: {}, eventType: {}, source: {}, externalId: {}",
                tenantId, eventType, source, externalId);

        if (externalId != null && !externalId.isBlank()) {
            Optional<Event> existing = eventRepository.findByExternalId(tenantId, externalId);

            if (existing.isPresent()) {
                log.info("Duplicate event detected, externalId: {}, existingEventId: {}",
                        externalId, existing.get().id());
                return existing.get();
            }
        }

        Event event = Event.builder()
                .tenantId(tenantId)
                .eventType(eventType)
                .source(source)
                .externalId(externalId)
                .payload(payload)
                .status(EventStatus.PENDING)
                .receivedAt(Instant.now())
                .build();

        Event savedEvent = eventRepository.save(event);

        log.info("Event ingested successfully, eventId: {}, tenantId: {}", savedEvent.id(), tenantId);

        triggerAsyncProcessing(savedEvent);

        return savedEvent;
    }

    @Override
    public Optional<Event> findById(String eventId) {

        return eventRepository.findById(eventId);
    }

    @Override
    public List<Event> findByTenantId(String tenantId) {

        return eventRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Event> findByTenantIdAndStatus(String tenantId, EventStatus status) {

        return eventRepository.findByTenantIdAndStatus(tenantId, status);
    }

    @Override
    public Event updateStatus(String eventId, EventStatus status, String category, Integer priority, String filterReasoning) {

        log.info("Updating event status, eventId: {}, status: {}, category: {}", eventId, status, category);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found, eventId: " + eventId));

        Event updatedEvent = event.toBuilder()
                .status(status)
                .category(category)
                .priority(priority)
                .filterReasoning(filterReasoning)
                .processedAt(Instant.now())
                .build();

        return eventRepository.save(updatedEvent);
    }

    @Override
    public Event markAsFailed(String eventId, String errorMessage) {

        log.error("Marking event as failed, eventId: {}, error: {}", eventId, errorMessage);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found, eventId: " + eventId));

        Event failedEvent = event.markAsFailed(errorMessage);

        return eventRepository.save(failedEvent);
    }

    @Override
    public List<Event> getPendingEvents(String tenantId, int limit) {

        return eventRepository.findPendingEvents(tenantId, limit);
    }

    @Override
    public long countByStatus(String tenantId, EventStatus status) {

        return eventRepository.countByTenantIdAndStatus(tenantId, status);
    }

    @Async("eventProcessorExecutor")
    protected void triggerAsyncProcessing(Event event) {

        log.debug("Triggering async processing for event, eventId: {}", event.id());

        // TODO: Integrate with FilterAgent for event classification
        // FilterAgent.filter(event) -> FilterResult
        // If actionable -> create workflow
        // If filtered -> mark as filtered
    }

}
