package com.agentica.agents.node;

import com.agentica.core.state.AgenticaState;

import java.util.Map;

/**
 * Interface for agent nodes that can be executed as part of a LangGraph4j workflow.
 * Each implementation wraps a specific agent type and handles execution with escalation support.
 */
public interface AgentNode {

    /**
     * Executes the agent with the given state and configuration.
     *
     * @param state the current workflow state
     * @param instruction optional instruction override (may be null)
     * @param config optional configuration parameters (may be null)
     * @return the result of execution, including output or escalation info
     */
    AgentNodeResult execute(AgenticaState state, String instruction, Map<String, Object> config);

    /**
     * Gets the name of this agent node.
     *
     * @return the agent node name
     */
    String getName();

    /**
     * Gets the description of what this agent node does.
     *
     * @return the agent node description
     */
    String getDescription();

}
