package com.agentica.infrastructure.persistence.repository;

import com.agentica.core.domain.Event;
import com.agentica.core.enums.EventStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of EventRepository.
 * Suitable for development and testing. Replace with JPA implementation for production.
 */
@Slf4j
@Repository
public class InMemoryEventRepository implements EventRepository {

    private final Map<String, Event> events = new ConcurrentHashMap<>();

    @Override
    public Event save(Event event) {

        String id = event.id();

        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
            event = event.toBuilder().id(id).build();
        }

        events.put(id, event);
        log.debug("Saved event, id: {}, type: {}, source: {}", id, event.eventType(), event.source());

        return event;
    }

    @Override
    public Optional<Event> findById(String id) {

        return Optional.ofNullable(events.get(id));
    }

    @Override
    public Optional<Event> findByExternalId(String tenantId, String externalId) {

        return events.values().stream()
                .filter(e -> tenantId.equals(e.tenantId()))
                .filter(e -> externalId.equals(e.externalId()))
                .findFirst();
    }

    @Override
    public List<Event> findByTenantId(String tenantId) {

        return events.values().stream()
                .filter(e -> tenantId.equals(e.tenantId()))
                .sorted(Comparator.comparing(Event::receivedAt).reversed())
                .toList();
    }

    @Override
    public List<Event> findByTenantIdAndStatus(String tenantId, EventStatus status) {

        return events.values().stream()
                .filter(e -> tenantId.equals(e.tenantId()))
                .filter(e -> status.equals(e.status()))
                .sorted(Comparator.comparing(Event::receivedAt).reversed())
                .toList();
    }

    @Override
    public List<Event> findByTenantIdAndSource(String tenantId, String source) {

        return events.values().stream()
                .filter(e -> tenantId.equals(e.tenantId()))
                .filter(e -> source.equals(e.source()))
                .sorted(Comparator.comparing(Event::receivedAt).reversed())
                .toList();
    }

    @Override
    public List<Event> findPendingEvents(String tenantId, int limit) {

        return events.values().stream()
                .filter(e -> tenantId.equals(e.tenantId()))
                .filter(e -> EventStatus.PENDING.equals(e.status()))
                .sorted(Comparator.comparing(Event::receivedAt))
                .limit(limit)
                .toList();
    }

    @Override
    public long countByTenantIdAndStatus(String tenantId, EventStatus status) {

        return events.values().stream()
                .filter(e -> tenantId.equals(e.tenantId()))
                .filter(e -> status.equals(e.status()))
                .count();
    }

    @Override
    public void deleteById(String id) {

        events.remove(id);
        log.debug("Deleted event, id: {}", id);
    }

    @Override
    public void deleteByTenantId(String tenantId) {

        List<String> idsToRemove = events.values().stream()
                .filter(e -> tenantId.equals(e.tenantId()))
                .map(Event::id)
                .toList();

        idsToRemove.forEach(events::remove);
        log.debug("Deleted all events for tenant, tenantId: {}, count: {}", tenantId, idsToRemove.size());
    }

}
