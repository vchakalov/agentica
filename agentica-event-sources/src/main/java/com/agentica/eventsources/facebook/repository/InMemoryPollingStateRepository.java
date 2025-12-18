package com.agentica.eventsources.facebook.repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.agentica.eventsources.common.PollingState;

import lombok.extern.slf4j.Slf4j;

/**
 * In-memory implementation of PollingStateRepository.
 */
@Slf4j
@Repository
public class InMemoryPollingStateRepository implements PollingStateRepository {

    private final Map<String, PollingState> states = new ConcurrentHashMap<>();

    @Override
    public PollingState save(PollingState state) {

        PollingState stateToSave = state;

        if (state.id() == null) {

            stateToSave = state.toBuilder()
                    .id(UUID.randomUUID().toString())
                    .build();
        }

        states.put(stateToSave.id(), stateToSave);

        return stateToSave;
    }

    @Override
    public Optional<PollingState> findById(String id) {

        return Optional.ofNullable(states.get(id));
    }

    @Override
    public Optional<PollingState> findByTenantAndSource(String tenantId, String sourceType, String sourceId) {

        return states.values().stream()
                .filter(s -> s.tenantId().equals(tenantId))
                .filter(s -> s.sourceType().equals(sourceType))
                .filter(s -> s.sourceId().equals(sourceId))
                .findFirst();
    }

    @Override
    public void deleteAll() {

        log.info("Clearing all polling states, count: {}", states.size());

        states.clear();
    }

}
