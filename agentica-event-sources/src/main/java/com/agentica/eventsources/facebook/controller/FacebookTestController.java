package com.agentica.eventsources.facebook.controller;

import com.agentica.eventsources.facebook.client.FacebookGraphClient;
import com.agentica.eventsources.facebook.config.FacebookConfig;
import com.agentica.eventsources.facebook.domain.FacebookCommentsResponse;
import com.agentica.eventsources.facebook.scheduler.FacebookCommentPoller;
import com.agentica.eventsources.facebook.service.FacebookCredentialsProvider;
import com.agentica.eventsources.facebook.service.FacebookPollingStateService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller for manually triggering Facebook comment polling.
 * Only enabled in dev/test environments.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/test/facebook")
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "agentica.event-sources.facebook",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class FacebookTestController {

  private final FacebookCommentPoller facebookCommentPoller;

  private final FacebookGraphClient facebookGraphClient;

  private final FacebookCredentialsProvider credentialsProvider;

  private final FacebookPollingStateService pollingStateService;

  private final FacebookConfig config;


  @PostMapping("/poll")
  public ResponseEntity<Map<String, Object>> triggerPoll() {

    log.info("Manual Facebook poll triggered via API");

    final Map<String, Object> result = new HashMap<>();

    try {

      facebookCommentPoller.poll();

      result.put("status", "success");
      result.put("message", "Polling cycle completed - check logs for details");

      return ResponseEntity.ok(result);

    } catch (final Exception e) {

      log.error("Manual poll failed, error: {}", e.getMessage(), e);

      result.put("status", "error");
      result.put("message", e.getMessage());

      return ResponseEntity.internalServerError().body(result);
    }
  }

  /**
   * Fetches comments directly from Facebook without ingesting them.
   * Useful for testing credentials and seeing raw comment data.
   */
  @GetMapping("/comments")
  public ResponseEntity<FacebookCommentsResponse> fetchComments(
      @RequestParam final String pageId,
      @RequestParam final String accessToken) {

    log.info("Fetching Facebook comments for testing, pageId: {}", pageId);

    try {

      final FacebookCommentsResponse response = facebookGraphClient.getPageComments(
          pageId,
          accessToken,
          null,  // No since filter - get recent comments
          null   // No cursor - first page
      );

      log.info("Fetched {} comments from Facebook", response.comments().size());

      return ResponseEntity.ok(response);

    } catch (final Exception e) {

      log.error("Failed to fetch comments, error: {}", e.getMessage(), e);

      return ResponseEntity.internalServerError().body(FacebookCommentsResponse.empty());
    }
  }

  /**
   * Returns the current Facebook configuration.
   */
  @GetMapping("/config")
  public ResponseEntity<Map<String, Object>> getConfig() {

    final Map<String, Object> configInfo = new HashMap<>();

    configInfo.put("enabled", config.isEnabled());
    configInfo.put("graphApiVersion", config.getGraphApiVersion());
    configInfo.put("pollIntervalMs", config.getPollIntervalMs());
    configInfo.put("maxCommentsPerPoll", config.getMaxCommentsPerPoll());
    configInfo.put("initialLookbackHours", config.getInitialLookbackHours());
    configInfo.put("configuredPages", credentialsProvider.findAllActive().size());

    return ResponseEntity.ok(configInfo);
  }

  /**
   * Resets all polling state, allowing a fresh import of all comments.
   */
  @PostMapping("/reset")
  public ResponseEntity<Map<String, Object>> resetPollingState() {

    log.info("Resetting all polling state via API");

    final Map<String, Object> result = new HashMap<>();

    try {

      pollingStateService.resetAll();

      result.put("status", "success");
      result.put("message", "Polling state reset - next poll will import all available comments");

      return ResponseEntity.ok(result);

    } catch (final Exception e) {

      log.error("Failed to reset polling state, error: {}", e.getMessage(), e);

      result.put("status", "error");
      result.put("message", e.getMessage());

      return ResponseEntity.internalServerError().body(result);
    }
  }

}
