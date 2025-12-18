package com.agentica.eventsources.facebook.client;

import com.agentica.eventsources.common.EventSourceException;

/**
 * Exception thrown when Facebook API rate limit is exceeded.
 */
public class FacebookRateLimitException extends EventSourceException {

    private final int retryAfterSeconds;

    public FacebookRateLimitException(String message, int retryAfterSeconds) {

        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int getRetryAfterSeconds() {

        return retryAfterSeconds;
    }

}
