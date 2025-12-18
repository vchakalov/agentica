package com.agentica.agents.registry;

import com.agentica.agents.node.AgentNode;
import com.agentica.core.enums.AgentType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Registry of available agent nodes.
 * Maps AgentType to AgentNode implementations for use in workflow building.
 */
@Slf4j
@Component
public class AgentNodeRegistry {

  private final Map<AgentType, AgentNode> agents = new EnumMap<>(AgentType.class);

  /**
   * Constructs the registry with all available agent nodes.
   * Agent nodes are injected by Spring based on available implementations.
   *
   * @param agentNodes list of all available agent nodes
   */
  public AgentNodeRegistry(final List<AgentNode> agentNodes) {

    for (final AgentNode node : agentNodes) {

      final AgentType type = getAgentType(node);

      if (type != null) {

        agents.put(type, node);
      }
    }
  }

  /**
   * Gets the agent node for the given type.
   *
   * @param type the agent type
   * @return the agent node
   * @throws IllegalArgumentException if no agent is registered for the type
   */
  public AgentNode get(final AgentType type) {

    final AgentNode agent = agents.get(type);

    if (agent == null) {

      throw new IllegalArgumentException("No agent registered for type: " + type);
    }

    return agent;
  }

  /**
   * Checks if an agent is registered for the given type.
   *
   * @param type the agent type
   * @return true if an agent is registered
   */
  public boolean hasAgent(final AgentType type) {

    return agents.containsKey(type);
  }

  /**
   * Gets the list of available agent types.
   *
   * @return list of registered agent types
   */
  public List<AgentType> getAvailableAgents() {

    return List.copyOf(agents.keySet());
  }

  /**
   * Gets the count of registered agents.
   *
   * @return number of registered agents
   */
  public int size() {

    return agents.size();
  }

  private AgentType getAgentType(final AgentNode node) {

    final String name = node.getName().toUpperCase();

    for (final AgentType type : AgentType.values()) {

      if (name.contains(type.name())) {

        return type;
      }
    }

    log.warn("Could not determine AgentType for node: {}", node.getName());

    return null;
  }

}
