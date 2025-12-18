package com.agentica.eventsources.facebook.scheduler;

import com.agentica.core.service.EventService;
import com.agentica.eventsources.common.PollingState;
import com.agentica.eventsources.common.PollingStatus;
import com.agentica.eventsources.facebook.client.FacebookGraphClient;
import com.agentica.eventsources.facebook.client.FacebookRateLimitException;
import com.agentica.eventsources.facebook.config.FacebookConfig;
import com.agentica.eventsources.facebook.domain.FacebookComment;
import com.agentica.eventsources.facebook.domain.FacebookCommentsResponse;
import com.agentica.eventsources.facebook.domain.FacebookCredentials;
import com.agentica.eventsources.facebook.mapper.FacebookEventMapper;
import com.agentica.eventsources.facebook.service.FacebookCredentialsProvider;
import com.agentica.eventsources.facebook.service.FacebookPollingStateService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "agentica.event-sources.facebook",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class FacebookCommentPoller {

  private static final String EVENT_TYPE_NEW = "facebook.comment.new";

  private static final String EVENT_TYPE_THREAD_REPLY = "facebook.comment.thread_reply";

  private static final String EVENT_TYPE_USER_REPLY = "facebook.comment.user_reply";

  private static final String SOURCE = "facebook";

  private final FacebookCredentialsProvider credentialsProvider;

  private final FacebookPollingStateService pollingStateService;

  private final FacebookGraphClient facebookClient;

  private final FacebookEventMapper eventMapper;

  private final EventService eventService;

  private final FacebookConfig config;


  @Scheduled(fixedDelayString = "${agentica.event-sources.facebook.poll-interval-ms:60000}")
  public void poll() {

    if (!config.isEnabled()) {

      log.debug("Facebook polling is disabled");

      return;
    }

    log.info("Starting Facebook comment polling cycle");

    final List<FacebookCredentials> activeCredentials = credentialsProvider.findAllActive();

    if (activeCredentials.isEmpty()) {

      log.debug("No active Facebook credentials configured");

      return;
    }

    int totalProcessed = 0;

    for (final FacebookCredentials credentials : activeCredentials) {

      try {

        final int processed = pollForCredentials(credentials);

        totalProcessed += processed;

      } catch (final Exception e) {

        log.error("Failed to poll Facebook page, tenantId: {}, pageId: {}, error: {}",
            credentials.tenantId(), credentials.pageId(), e.getMessage(), e);

        pollingStateService.recordFailure(
            credentials.tenantId(),
            credentials.pageId(),
            e.getMessage()
        );
      }
    }

    log.info("Completed Facebook comment polling cycle, processedPages: {}, totalComments: {}",
        activeCredentials.size(), totalProcessed);
  }

  private int pollForCredentials(final FacebookCredentials credentials) {

    final PollingState state = pollingStateService.getOrCreate(
        credentials.tenantId(),
        credentials.pageId()
    );

    if (state.status() == PollingStatus.ERROR) {

      log.warn("Skipping polling for page in ERROR state, tenantId: {}, pageId: {}",
          credentials.tenantId(), credentials.pageId());

      return 0;
    }

    final Instant since = calculateSinceTimestamp(state);
    String cursor = state.lastCursor();

    int totalProcessed = 0;

    try {

      do {

        final FacebookCommentsResponse response = facebookClient.getPageComments(
            credentials.pageId(),
            credentials.accessToken(),
            since,
            cursor
        );

        for (final FacebookComment comment : response.comments()) {

          if (processComment(credentials, comment)) {

            totalProcessed++;
          }
        }

        cursor = response.nextCursor();

        if (cursor != null) {

          pollingStateService.updateCursor(state.id(), cursor);
        }

      } while (cursor != null && totalProcessed < config.getMaxCommentsPerPoll());

      pollingStateService.markPolled(state.id(), Instant.now());

      log.info("Polled Facebook page, tenantId: {}, pageId: {}, commentsProcessed: {}",
          credentials.tenantId(), credentials.pageId(), totalProcessed);

    } catch (final FacebookRateLimitException e) {

      log.warn("Facebook rate limit hit, tenantId: {}, pageId: {}, retryAfter: {}s",
          credentials.tenantId(), credentials.pageId(), e.getRetryAfterSeconds());

      pollingStateService.recordFailure(
          credentials.tenantId(),
          credentials.pageId(),
          "Rate limit exceeded, retry after " + e.getRetryAfterSeconds() + " seconds"
      );
    }

    return totalProcessed;
  }

  private Instant calculateSinceTimestamp(final PollingState state) {

    if (state.lastPolledAt() != null) {

      return state.lastPolledAt();
    }

    return null;
  }

  private boolean processComment(final FacebookCredentials credentials,
      final FacebookComment comment) {

    final String pageId = credentials.pageId();

    if (pageId.equals(comment.fromId())) {

      log.debug("Skipping self-comment, tenantId: {}, pageId: {}, commentId: {}",
          credentials.tenantId(), pageId, comment.id());

      return false;
    }

    final String commentType = classifyComment(comment, pageId);
    final String eventType = getEventTypeForCommentType(commentType);

    final FacebookComment classifiedComment = comment.toBuilder()
        .commentType(commentType)
        .build();

    final String externalId = "fb_comment_" + comment.id();

    final Map<String, Object> payload = eventMapper.toPayload(
        classifiedComment,
        credentials.pageId(),
        credentials.pageName()
    );

    eventService.ingest(
        credentials.tenantId(),
        eventType,
        SOURCE,
        externalId,
        payload
    );

    log.debug("Ingested Facebook comment as event, tenantId: {}, commentId: {}, type: {}",
        credentials.tenantId(), comment.id(), commentType);

    return true;
  }

  private String classifyComment(final FacebookComment comment, final String pageId) {

    if (comment.parentCommentId() == null) {

      return FacebookComment.TYPE_NEW;
    }

    if (pageId.equals(comment.parentFromId())) {

      return FacebookComment.TYPE_THREAD_REPLY;
    }

    return FacebookComment.TYPE_USER_REPLY;
  }


  private String getEventTypeForCommentType(final String commentType) {

    return switch (commentType) {
      case FacebookComment.TYPE_NEW -> EVENT_TYPE_NEW;
      case FacebookComment.TYPE_THREAD_REPLY -> EVENT_TYPE_THREAD_REPLY;
      case FacebookComment.TYPE_USER_REPLY -> EVENT_TYPE_USER_REPLY;
      default -> EVENT_TYPE_NEW;
    };
  }
}
