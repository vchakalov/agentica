package com.agentica.eventsources.facebook.client;

import com.agentica.eventsources.common.EventSourceException;
import com.agentica.eventsources.facebook.config.FacebookConfig;
import com.agentica.eventsources.facebook.domain.FacebookComment;
import com.agentica.eventsources.facebook.domain.FacebookCommentsResponse;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;


@Slf4j
@Component
public class FacebookGraphClientImpl implements FacebookGraphClient {

  private final WebClient webClient;

  private final FacebookConfig config;

  public FacebookGraphClientImpl(FacebookConfig config) {

    this.config = config;

    this.webClient = WebClient.builder()
        .baseUrl(config.getGraphApiBaseUrl() + "/" + config.getGraphApiVersion())
        .build();
  }

  @Override
  public FacebookCommentsResponse getPageComments(
      String pageId,
      String accessToken,
      Instant since,
      String cursor) {

    log.debug("Fetching comments from Facebook, pageId: {}, since: {}, cursor: {}",
        pageId, since, cursor != null ? "present" : "null");

    try {

      URI uri = buildCommentsUri(pageId, accessToken, since, cursor);

      log.debug("Calling Facebook API, uri: {}",
          uri.toString().replaceAll("access_token=[^&]+", "access_token=***"));

      JsonNode response = webClient.get()
          .uri(uri)
          .retrieve()
          .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {

            if (clientResponse.statusCode().value() == 429) {

              String retryAfter = clientResponse.headers()
                  .header("Retry-After")
                  .stream()
                  .findFirst()
                  .orElse("60");

              return clientResponse.createError()
                  .map(e -> new FacebookRateLimitException(
                      "Rate limit exceeded",
                      Integer.parseInt(retryAfter)));
            }

            return clientResponse.createError();
          })
          .bodyToMono(JsonNode.class)
          .timeout(Duration.ofMillis(config.getReadTimeoutMs()))
          .block();

      return parseCommentsResponse(response, pageId);

    } catch (FacebookRateLimitException e) {

      throw e;

    } catch (WebClientResponseException e) {

      log.error("Facebook API error, pageId: {}, status: {}, body: {}",
          pageId, e.getStatusCode(), e.getResponseBodyAsString(), e);

      throw new EventSourceException("Facebook API call failed: " + e.getMessage(), e);

    } catch (Exception e) {

      log.error("Failed to fetch Facebook comments, pageId: {}, error: {}",
          pageId, e.getMessage(), e);

      throw new EventSourceException("Failed to fetch Facebook comments", e);
    }
  }

  @Override
  public boolean validateToken(String accessToken) {

    try {

      webClient.get()
          .uri(uriBuilder -> uriBuilder
              .path("/me")
              .queryParam("access_token", accessToken)
              .build())
          .retrieve()
          .bodyToMono(JsonNode.class)
          .timeout(Duration.ofMillis(config.getConnectionTimeoutMs()))
          .block();

      return true;

    } catch (Exception e) {

      log.warn("Token validation failed, error: {}", e.getMessage());

      return false;
    }
  }

  private URI buildCommentsUri(
      String pageId,
      String accessToken,
      Instant since,
      String cursor) {

    StringBuilder url = new StringBuilder();

    url.append(config.getGraphApiBaseUrl());
    url.append("/").append(config.getGraphApiVersion());
    url.append("/").append(pageId).append("/feed");
    url.append(
        "?fields=id,message,created_time,comments.limit(100)%7Bid,from,message,created_time,parent%7Bid,from%7D,comment_count,like_count%7D");
    url.append("&access_token=").append(accessToken);

    if (since != null) {

      url.append("&since=").append(since.getEpochSecond());
    }

    if (cursor != null && !cursor.isBlank()) {

      url.append("&after=").append(cursor);
    }

    return URI.create(url.toString());
  }

  private FacebookCommentsResponse parseCommentsResponse(JsonNode response, String pageId) {

    List<FacebookComment> comments = new ArrayList<>();
    String nextCursor = null;

    boolean hasMore = false;

    if (response == null) {

      return FacebookCommentsResponse.empty();
    }

    JsonNode dataNode = response.get("data");

    if (dataNode != null && dataNode.isArray()) {

      for (JsonNode postNode : dataNode) {

        String postId = postNode.has("id") ? postNode.get("id").asText() : null;
        JsonNode commentsNode = postNode.get("comments");

        if (commentsNode != null) {

          JsonNode commentsData = commentsNode.get("data");

          if (commentsData != null && commentsData.isArray()) {

            for (JsonNode commentNode : commentsData) {

              FacebookComment comment = parseComment(commentNode, postId);

              if (comment != null) {
                comments.add(comment);
              }
            }
          }

          JsonNode commentsPaging = commentsNode.get("paging");

          if (commentsPaging != null && commentsPaging.has("next")) {
            hasMore = true;
          }
        }
      }
    }

    JsonNode pagingNode = response.get("paging");

    if (pagingNode != null) {

      JsonNode cursorsNode = pagingNode.get("cursors");

      if (cursorsNode != null && cursorsNode.has("after")) {
        nextCursor = cursorsNode.get("after").asText();
      }

      if (pagingNode.has("next")) {
        hasMore = true;
      }
    }

    log.debug("Parsed Facebook response, commentsCount: {}, hasMore: {}", comments.size(), hasMore);

    return FacebookCommentsResponse.builder()
        .comments(comments)
        .nextCursor(nextCursor)
        .hasMore(hasMore)
        .build();
  }

  private FacebookComment parseComment(JsonNode commentNode, String postId) {

    try {

      String id = commentNode.has("id") ? commentNode.get("id").asText() : null;

      if (id == null) {
        return null;
      }

      String message = commentNode.has("message") ? commentNode.get("message").asText() : "";

      String fromId = null;
      String fromName = null;
      JsonNode fromNode = commentNode.get("from");

      if (fromNode != null) {
        fromId = fromNode.has("id") ? fromNode.get("id").asText() : null;
        fromName = fromNode.has("name") ? fromNode.get("name").asText() : null;
      }

      Instant createdTime = null;

      if (commentNode.has("created_time")) {
        createdTime = parseFacebookDateTime(commentNode.get("created_time").asText());
      }

      String parentCommentId = null;
      String parentFromId = null;
      JsonNode parentNode = commentNode.get("parent");

      if (parentNode != null) {

        if (parentNode.has("id")) {
          parentCommentId = parentNode.get("id").asText();
        }

        JsonNode parentFromNode = parentNode.get("from");

        if (parentFromNode != null && parentFromNode.has("id")) {
          parentFromId = parentFromNode.get("id").asText();
        }
      }

      int commentCount = 0;

      if (commentNode.has("comment_count")) {
        commentCount = commentNode.get("comment_count").asInt(0);
      }

      int likeCount = 0;

      if (commentNode.has("like_count")) {
        likeCount = commentNode.get("like_count").asInt(0);
      }

      return FacebookComment.builder()
          .id(id)
          .postId(postId)
          .message(message)
          .fromId(fromId)
          .fromName(fromName)
          .createdTime(createdTime)
          .parentCommentId(parentCommentId)
          .parentFromId(parentFromId)
          .commentCount(commentCount)
          .likeCount(likeCount)
          .build();

    } catch (Exception e) {

      log.warn("Failed to parse comment, error: {}", e.getMessage());

      return null;
    }
  }

  private Instant parseFacebookDateTime(String dateTimeStr) {

    if (dateTimeStr == null || dateTimeStr.isBlank()) {

      return null;
    }

    try {

      return Instant.parse(dateTimeStr);

    } catch (DateTimeParseException e) {

      if (dateTimeStr.length() >= 24 && !dateTimeStr.contains(":00:00") &&
          (dateTimeStr.endsWith("+0000") || dateTimeStr.matches(".*[+-]\\d{4}$"))) {

        String fixed = dateTimeStr.substring(0, dateTimeStr.length() - 2) + ":" +
            dateTimeStr.substring(dateTimeStr.length() - 2);

        return Instant.parse(fixed);
      }

      throw e;
    }
  }

}
