package com.agentica.eventsources.facebook.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;

/**
 * Response from the Facebook Graph API containing comments.
 */
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record FacebookCommentsResponse(

        List<FacebookComment> comments,

        String nextCursor,

        boolean hasMore

) {

    public static FacebookCommentsResponse empty() {

        return FacebookCommentsResponse.builder()
                .comments(List.of())
                .hasMore(false)
                .build();
    }

}
