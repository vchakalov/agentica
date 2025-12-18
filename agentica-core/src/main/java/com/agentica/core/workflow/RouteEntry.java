package com.agentica.core.workflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;

import java.io.Serializable;

/**
 * A single route entry for conditional workflow routing.
 * Maps a state value to a target node ID.
 */
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record RouteEntry(

    /**
     * The state value to match.
     */
    String value,

    /**
     * The target node ID to route to when the value matches.
     */
    String target

) implements Serializable {}
