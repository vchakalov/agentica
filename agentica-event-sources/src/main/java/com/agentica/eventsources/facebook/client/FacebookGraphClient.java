package com.agentica.eventsources.facebook.client;

import java.time.Instant;

import com.agentica.eventsources.facebook.domain.FacebookCommentsResponse;

/**
 * Client interface for Facebook Graph API operations.
 */
public interface FacebookGraphClient {

    /**
     * Fetches comments for a page's posts.
     *
     * @param pageId      the Facebook page ID
     * @param accessToken the page access token
     * @param since       fetch comments created after this time
     * @param cursor      pagination cursor (null for first page)
     * @return paginated response with comments
     */
    FacebookCommentsResponse getPageComments(
            String pageId,
            String accessToken,
            Instant since,
            String cursor
    );

    /**
     * Validates the access token.
     *
     * @param accessToken the access token to validate
     * @return true if valid, false otherwise
     */
    boolean validateToken(String accessToken);

}
