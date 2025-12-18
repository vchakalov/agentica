package com.agentica.eventsources.facebook.domain;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;

/**
 * Credentials for accessing a Facebook page via the Graph API.
 */
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record FacebookCredentials(

        String id,

        @NotBlank
        String tenantId,

        @NotBlank
        String pageId,

        String pageName,

        @NotBlank
        String accessToken,

        Instant tokenExpiresAt,

        boolean enabled,

        Instant createdAt,

        Instant updatedAt

) {

    /**
     * Creates new credentials for a Facebook page.
     */
    public static FacebookCredentials create(
            String tenantId,
            String pageId,
            String pageName,
            String accessToken) {

        Instant now = Instant.now();

        return FacebookCredentials.builder()
                .tenantId(tenantId)
                .pageId(pageId)
                .pageName(pageName)
                .accessToken(accessToken)
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

}
