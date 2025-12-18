package com.agentica.core.workflow;

/**
 * Types of edges in a workflow graph.
 */
public enum EdgeType {

    /**
     * Direct edge that always routes to the target node.
     */
    DIRECT,

    /**
     * Conditional edge that routes based on state evaluation.
     */
    CONDITIONAL

}
