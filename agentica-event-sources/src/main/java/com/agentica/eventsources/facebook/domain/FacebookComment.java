package com.agentica.eventsources.facebook.domain;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;

/**
 * Represents a comment from a Facebook page post.
 */
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record FacebookComment(

        String id,

        String postId,

        String message,

        String fromId,

        String fromName,

        Instant createdTime,

        String parentCommentId,

        String parentFromId,

        int commentCount,

        int likeCount,

        String commentType

) {

    /**
     * Comment type classification constants.
     */
    public static final String TYPE_NEW = "new";

    public static final String TYPE_THREAD_REPLY = "thread_reply";

    public static final String TYPE_USER_REPLY = "user_reply";

}
