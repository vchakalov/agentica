package com.agentica.agents.node;

import com.agentica.agents.config.AdkConfig;
import com.agentica.agents.session.AdkSessionManager;
import com.agentica.common.util.JsonUtils;
import com.agentica.core.domain.Event;
import com.agentica.core.state.AgenticaState;

import com.google.adk.agents.LlmAgent;

import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Marketing agent node for sentiment analysis and response planning.
 */
@Slf4j
@Component
public class MarketingAgentNode extends BaseAgentNode {

    private static final String MARKETING_INSTRUCTION = """
        You are a Marketing Agent specialized in customer engagement analysis.

        Your responsibilities:
        - Analyze customer sentiment (positive, neutral, negative)
        - Categorize customer intent (question, complaint, feedback, spam)
        - Recommend response strategy (reply, escalate, delete)
        - Draft appropriate response messages

        Output your analysis in a structured format:
        - sentiment: The detected sentiment
        - intent: The customer intent category
        - urgency: How urgent is the response (low, medium, high)
        - recommendedAction: What action to take
        - suggestedResponse: Draft response if applicable
        - escalationNeeded: Whether human review is needed

        Be professional and customer-focused in all recommendations.
        """;

    public MarketingAgentNode(AdkConfig adkConfig, AdkSessionManager sessionManager) {

        super(adkConfig, sessionManager);
    }

    @PostConstruct
    public void init() {

        log.info("Initializing MarketingAgentNode...");

        this.agent = LlmAgent.builder()
            .name("marketing_agent")
            .description("Analyzes customer sentiment and plans marketing responses")
            .model(adkConfig.getDefaultModel())
            .instruction(MARKETING_INSTRUCTION)
            .build();

        log.info("MarketingAgentNode initialized");
    }

    @Override
    public String getName() {

        return "marketing_agent";
    }

    @Override
    public String getDescription() {

        return "Analyzes customer sentiment and plans marketing responses";
    }

    @Override
    protected String buildPrompt(AgenticaState state, Map<String, Object> config) {

        Event event = state.event()
            .orElseThrow(() -> new IllegalStateException("Event not found in state"));

        String payloadJson = JsonUtils.toPrettyJson(event.payload())
            .orElse("{}");

        return String.format("""
            Analyze this customer interaction:

            Event Type: %s
            Source: %s

            Content:
            %s

            Previous Node Outputs:
            %s

            Provide your analysis with sentiment, intent, urgency, recommended action,
            suggested response, and whether escalation is needed.
            """,
            event.eventType(),
            event.source(),
            payloadJson,
            JsonUtils.toPrettyJson(state.nodeOutputs()).orElse("{}")
        );
    }

    @Override
    protected AgentNodeResult processResult(String result, AgenticaState state) {

        if (result.toLowerCase().contains("\"escalationneeded\":true") ||
            result.toLowerCase().contains("\"escalation_needed\":true") ||
            result.toLowerCase().contains("escalation: true")) {

            log.info("Marketing agent recommends escalation");

            return AgentNodeResult.escalate(
                "APPROVAL_NEEDED",
                "Marketing analysis suggests human review is needed: " + result
            );
        }

        return AgentNodeResult.success(result);
    }

}
