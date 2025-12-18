package com.agentica.eventsources.facebook.service;

import com.agentica.eventsources.common.PollingState;
import com.agentica.eventsources.common.PollingStatus;
import com.agentica.eventsources.facebook.repository.PollingStateRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacebookPollingStateService {

  private static final String SOURCE_TYPE = "FACEBOOK";

  private final PollingStateRepository pollingStateRepository;

  public PollingState getOrCreate(String tenantId, String pageId) {

    return pollingStateRepository.findByTenantAndSource(tenantId, SOURCE_TYPE, pageId)
        .orElseGet(() -> {

          PollingState newState = PollingState.createNew(tenantId, SOURCE_TYPE, pageId);

          return pollingStateRepository.save(newState);
        });
  }

  public PollingState markPolled(String stateId, Instant polledAt) {

    PollingState state = pollingStateRepository.findById(stateId)
        .orElseThrow(() -> new IllegalArgumentException("Polling state not found: " + stateId));

    PollingState updatedState = state.markPolled(polledAt);

    log.debug("Marked polling state as polled, stateId: {}, polledAt: {}", stateId, polledAt);

    return pollingStateRepository.save(updatedState);
  }


  public PollingState updateCursor(String stateId, String cursor) {

    PollingState state = pollingStateRepository.findById(stateId)
        .orElseThrow(() -> new IllegalArgumentException("Polling state not found: " + stateId));

    PollingState updatedState = state.withCursor(cursor);

    return pollingStateRepository.save(updatedState);
  }

  public PollingState recordFailure(String tenantId, String pageId, String errorMessage) {

    PollingState state = getOrCreate(tenantId, pageId);
    PollingState updatedState = state.recordFailure(errorMessage);

    log.warn("Recorded polling failure, tenantId: {}, pageId: {}, failures: {}",
        tenantId, pageId, updatedState.consecutiveFailures());

    return pollingStateRepository.save(updatedState);
  }

  public PollingState updateStatus(String stateId, PollingStatus status) {

    PollingState state = pollingStateRepository.findById(stateId)
        .orElseThrow(() -> new IllegalArgumentException("Polling state not found: " + stateId));

    PollingState updatedState = state.toBuilder()
        .status(status)
        .updatedAt(Instant.now())
        .build();

    log.info("Updated polling state status, stateId: {}, status: {}", stateId, status);

    return pollingStateRepository.save(updatedState);
  }

  public void resetAll() {

    log.info("Resetting all polling states");

    pollingStateRepository.deleteAll();
  }
}
