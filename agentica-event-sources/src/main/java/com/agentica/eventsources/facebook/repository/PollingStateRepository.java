package com.agentica.eventsources.facebook.repository;

import java.util.Optional;

import com.agentica.eventsources.common.PollingState;

/**
 * Repository for managing polling state.
 */
public interface PollingStateRepository {

    /**
     * Saves a polling state.
     *
     * @param state the polling state to save
     * @return the saved state with generated ID
     */
    PollingState save(PollingState state);

    /**
     * Finds a polling state by ID.
     *
     * @param id the state ID
     * @return the state if found
     */
    Optional<PollingState> findById(String id);

    /**
     * Finds a polling state by tenant, source type, and source ID.
     *
     * @param tenantId   the tenant ID
     * @param sourceType the source type (e.g., "FACEBOOK")
     * @param sourceId   the source ID (e.g., page ID)
     * @return the state if found
     */
    Optional<PollingState> findByTenantAndSource(String tenantId, String sourceType, String sourceId);

    /**
     * Deletes all polling states.
     */
    void deleteAll();

}
