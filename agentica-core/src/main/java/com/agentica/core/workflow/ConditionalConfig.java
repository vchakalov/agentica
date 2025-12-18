package com.agentica.core.workflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Configuration for conditional routing in workflow edges.
 * Defines how to evaluate state and route to different target nodes.
 */
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ConditionalConfig(

    /**
     * The state key to evaluate for conditional routing.
     */
    String stateKey,

    /**
     * List of route entries mapping state values to target node IDs.
     */
    List<RouteEntry> routes

) implements Serializable {

    /**
     * Converts the routes list to a map for easier lookup.
     *
     * @return Map of state values to target node IDs
     */
    public Map<String, String> routesAsMap() {

        if (routes == null || routes.isEmpty()) {

            return Map.of();
        }

        return routes.stream()
            .collect(Collectors.toMap(RouteEntry::value, RouteEntry::target));
    }

}
