package com.agentica.eventsources.facebook.mapper;

import com.agentica.eventsources.facebook.domain.FacebookComment;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Maps Facebook comments to Event payloads.
 */
@Component
public class FacebookEventMapper {

  public Map<String, Object> toPayload(FacebookComment comment, String pageId, String pageName) {

    Map<String, Object> payload = new HashMap<>();

    payload.put("commentId", comment.id());
    payload.put("postId", comment.postId());
    payload.put("message", comment.message());
    payload.put("pageId", pageId);
    payload.put("pageName", pageName);

    if (comment.fromId() != null) {

      payload.put("fromId", comment.fromId());
    }

    if (comment.fromName() != null) {

      payload.put("fromName", comment.fromName());
    }

    if (comment.createdTime() != null) {

      payload.put("createdTime", comment.createdTime().toString());
    }

    if (comment.parentCommentId() != null) {

      payload.put("parentCommentId", comment.parentCommentId());
      payload.put("isReply", true);

    } else {

      payload.put("isReply", false);
    }

    if (comment.parentFromId() != null) {

      payload.put("parentFromId", comment.parentFromId());
    }

    payload.put("commentCount", comment.commentCount());
    payload.put("likeCount", comment.likeCount());

    if (comment.commentType() != null) {
            
      payload.put("commentType", comment.commentType());
    }

    return payload;
  }

}
