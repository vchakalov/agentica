package com.agentica.agents.session;

import com.agentica.core.domain.Event;
import com.google.adk.sessions.InMemorySessionService;
import com.google.adk.sessions.Session;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Manages ADK session lifecycle for workflow orchestration.
 * Uses in-memory session storage for MVP.
 */
@Slf4j
@Component
public class AdkSessionManager {

  private static final String APP_NAME = "agentica";

  private final InMemorySessionService sessionService;

  public AdkSessionManager() {
    this.sessionService = new InMemorySessionService();
  }

  /**
   * Creates a new ADK session for a workflow.
   *
   * @param workflowId unique identifier for the workflow
   * @param event      the event being processed
   * @return the created session
   */
  public Session createSession(String workflowId, Event event) {

    log.info("Creating ADK session, workflowId: {}, eventId: {}", workflowId, event.id());

    Map<String, Object> initialState = new ConcurrentHashMap<>();

    initialState.put("event_id", event.id());
    initialState.put("event_type", event.eventType());
    initialState.put("event_source", event.source());
    initialState.put("event_payload", event.payload());
    initialState.put("category", event.category());
    initialState.put("priority", event.priority());
    initialState.put("workflow_status", "PLANNING");
    initialState.put("workflow_id", workflowId);

    Session session = sessionService.createSession(
        APP_NAME,
        event.tenantId(),
        new ConcurrentHashMap<>(initialState),
        workflowId
    ).blockingGet();

    log.debug("ADK session created, sessionId: {}, tenantId: {}",
        session.id(), event.tenantId());

    return session;
  }

  /**
   * Retrieves an existing session by ID.
   *
   * @param tenantId  the tenant identifier
   * @param sessionId the session identifier
   * @return the session if found
   */
  public Optional<Session> getSession(String tenantId, String sessionId) {

    try {

      Session session = sessionService.getSession(
          APP_NAME,
          tenantId,
          sessionId,
          Optional.empty()
      ).blockingGet();

      return Optional.ofNullable(session);

    } catch (Exception e) {

      log.warn("Session not found, tenantId: {}, sessionId: {}", tenantId, sessionId);

      return Optional.empty();
    }
  }

  /**
   * Updates the session state with new values.
   *
   * @param session the session to update
   * @param updates map of state updates to apply
   */
  public void updateSessionState(Session session, Map<String, Object> updates) {

    session.state().putAll(updates);

    log.debug("Session state updated, sessionId: {}, keys: {}",
        session.id(), updates.keySet());
  }

  /**
   * Returns the underlying session service for use with InMemoryRunner.
   *
   * @return the session service
   */
  public InMemorySessionService getSessionService() {
    return sessionService;
  }

}
