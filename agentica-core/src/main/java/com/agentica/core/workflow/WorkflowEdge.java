package com.agentica.core.workflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Builder;

import java.io.Serializable;

/**
 * Represents an edge (transition) between nodes in a workflow graph.
 * Can be either a direct edge or a conditional edge based on state.
 */
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record WorkflowEdge(

    /**
     * The source node ID.
     */
    @NotBlank
    String from,

    /**
     * The target node ID for direct edges.
     * For conditional edges, this is the default target.
     */
    @NotBlank
    String to,

    /**
     * The type of edge (DIRECT or CONDITIONAL).
     */
    @NotNull
    EdgeType type,

    /**
     * Configuration for conditional routing.
     * Only used when type is CONDITIONAL.
     */
    ConditionalConfig condition

) implements Serializable {}
