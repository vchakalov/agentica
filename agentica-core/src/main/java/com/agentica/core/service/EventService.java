package com.agentica.core.service;

import com.agentica.core.domain.Event;
import com.agentica.core.enums.EventStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for event management.
 * Handles event ingestion, storage, and lifecycle management.
 */
public interface EventService {

    /**
     * Ingests a new event from an external source.
     *
     * @param tenantId   the tenant identifier
     * @param eventType  the type of event
     * @param source     the source system (e.g., whatsapp, facebook, hookdeck)
     * @param externalId optional external identifier for deduplication
     * @param payload    the event payload data
     * @return the created event with generated ID
     */
    Event ingest(String tenantId, String eventType, String source, String externalId, Map<String, Object> payload);

    /**
     * Finds an event by its ID.
     *
     * @param eventId the event ID
     * @return the event if found
     */
    Optional<Event> findById(String eventId);

    /**
     * Finds all events for a tenant.
     *
     * @param tenantId the tenant identifier
     * @return list of events
     */
    List<Event> findByTenantId(String tenantId);

    /**
     * Finds events by tenant and status.
     *
     * @param tenantId the tenant identifier
     * @param status   the event status
     * @return list of matching events
     */
    List<Event> findByTenantIdAndStatus(String tenantId, EventStatus status);

    /**
     * Updates an event's status and classification.
     *
     * @param eventId         the event ID
     * @param status          the new status
     * @param category        optional classification category
     * @param priority        optional priority level
     * @param filterReasoning optional reasoning for the classification
     * @return the updated event
     */
    Event updateStatus(String eventId, EventStatus status, String category, Integer priority, String filterReasoning);

    /**
     * Marks an event as failed with an error message.
     *
     * @param eventId      the event ID
     * @param errorMessage the error message
     * @return the updated event
     */
    Event markAsFailed(String eventId, String errorMessage);

    /**
     * Gets pending events ready for processing.
     *
     * @param tenantId the tenant identifier
     * @param limit    maximum number of events to return
     * @return list of pending events
     */
    List<Event> getPendingEvents(String tenantId, int limit);

    /**
     * Counts events by tenant and status.
     *
     * @param tenantId the tenant identifier
     * @param status   the event status
     * @return the count
     */
    long countByStatus(String tenantId, EventStatus status);

}
