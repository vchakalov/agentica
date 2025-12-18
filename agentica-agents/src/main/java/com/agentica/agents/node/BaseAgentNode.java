package com.agentica.agents.node;

import com.agentica.agents.config.AdkConfig;
import com.agentica.agents.session.AdkSessionManager;
import com.agentica.core.domain.Event;
import com.agentica.core.state.AgenticaState;

import com.agentica.agents.runner.AgenticaRunner;

import com.google.adk.agents.LlmAgent;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

/**
 * Base implementation for agent nodes providing common execution logic.
 * Subclasses should implement agent-specific behavior.
 */
@Slf4j
public abstract class BaseAgentNode implements AgentNode {

    protected final AdkConfig adkConfig;

    protected final AdkSessionManager sessionManager;

    protected LlmAgent agent;

    protected BaseAgentNode(AdkConfig adkConfig, AdkSessionManager sessionManager) {

        this.adkConfig = adkConfig;
        this.sessionManager = sessionManager;
    }

    @Override
    public AgentNodeResult execute(AgenticaState state, String instruction, Map<String, Object> config) {

        log.info("Executing agent node, name: {}, instruction: {}",
            getName(), instruction != null ? "custom" : "default");

        try {

            Event event = state.event()
                .orElseThrow(() -> new IllegalStateException("Event not found in state"));

            String sessionId = UUID.randomUUID().toString();

            Session session = sessionManager.createSession(sessionId, event);

            LlmAgent executionAgent = getExecutionAgent(instruction);

            Runner runner = new AgenticaRunner(
                executionAgent,
                "agentica",
                sessionManager.getSessionService()
            );

            String prompt = buildPrompt(state, config);

            Content userMessage = Content.fromParts(Part.fromText(prompt));

            StringBuilder responseText = new StringBuilder();

            for (com.google.adk.events.Event agentEvent :
                    runner.runAsync(event.tenantId(), session.id(), userMessage).blockingIterable()) {

                if (agentEvent.content().isPresent()) {

                    Content content = agentEvent.content().get();

                    if (content.parts().isPresent()) {

                        for (Part part : content.parts().get()) {

                            if (part.text().isPresent()) {

                                responseText.append(part.text().get());
                            }
                        }
                    }
                }
            }

            String result = responseText.toString().trim();

            log.debug("Agent node execution completed, name: {}, result length: {}",
                getName(), result.length());

            return processResult(result, state);

        } catch (Exception e) {

            log.error("Agent node execution failed, name: {}, error: {}",
                getName(), e.getMessage(), e);

            return AgentNodeResult.fromException(e);
        }
    }

    /**
     * Gets the agent to use for execution.
     * If a custom instruction is provided, a modified agent is created.
     *
     * @param instruction optional custom instruction
     * @return the agent to use for execution
     */
    protected LlmAgent getExecutionAgent(String instruction) {

        if (instruction == null || instruction.isBlank()) {

            return agent;
        }

        return LlmAgent.builder()
            .name(agent.name() + "_custom")
            .description(agent.description())
            .model(adkConfig.getDefaultModel())
            .instruction(instruction)
            .build();
    }

    /**
     * Builds the prompt for the agent based on current state.
     *
     * @param state the current workflow state
     * @param config optional configuration
     * @return the prompt string
     */
    protected abstract String buildPrompt(AgenticaState state, Map<String, Object> config);

    /**
     * Processes the result from agent execution.
     * Subclasses can override to add escalation logic.
     *
     * @param result the raw result from the agent
     * @param state the current workflow state
     * @return the processed AgentNodeResult
     */
    protected AgentNodeResult processResult(String result, AgenticaState state) {

        return AgentNodeResult.success(result);
    }

}
