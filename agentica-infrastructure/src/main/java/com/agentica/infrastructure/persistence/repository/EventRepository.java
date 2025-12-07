package com.agentica.infrastructure.persistence.repository;

import com.agentica.core.domain.Event;
import com.agentica.core.enums.EventStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Event persistence.
 * Provides abstraction over storage mechanism (in-memory, database, etc.)
 */
public interface EventRepository {

    Event save(Event event);

    Optional<Event> findById(String id);

    Optional<Event> findByExternalId(String tenantId, String externalId);

    List<Event> findByTenantId(String tenantId);

    List<Event> findByTenantIdAndStatus(String tenantId, EventStatus status);

    List<Event> findByTenantIdAndSource(String tenantId, String source);

    List<Event> findPendingEvents(String tenantId, int limit);

    long countByTenantIdAndStatus(String tenantId, EventStatus status);

    void deleteById(String id);

    void deleteByTenantId(String tenantId);

}
