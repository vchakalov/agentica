package com.agentica.core.workflow;

import com.agentica.core.enums.AgentType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Builder;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents a node in a workflow graph.
 * Each node corresponds to an agent execution step.
 */
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record WorkflowNode(

    /**
     * Unique identifier for this node within the workflow.
     */
    @NotBlank
    String id,

    /**
     * The type of agent to execute at this node.
     */
    @NotNull
    AgentType agentType,

    /**
     * Human-readable description of what this node does.
     */
    @NotBlank
    String description,

    /**
     * Optional instruction override for the agent.
     * If provided, this replaces the agent's default instruction.
     */
    String instruction,

    /**
     * Optional configuration parameters for the agent.
     */
    Map<String, Object> config

) implements Serializable {}
