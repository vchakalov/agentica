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
 * Review agent node for human-in-the-loop approval.
 * Always escalates to request human approval before proceeding.
 */
@Slf4j
@Component
public class ReviewAgentNode extends BaseAgentNode {

    private static final String REVIEW_INSTRUCTION = """
        You are a Review Agent for human-in-the-loop approval.

        Your role is to:
        1. Summarize the current workflow state and proposed actions
        2. Identify any risks or concerns
        3. Prepare the decision for human review
        4. Always escalate for human approval

        Output a review summary including:
        - workflowSummary: Brief description of what has been done
        - proposedAction: What action is pending approval
        - riskLevel: Assessment of risk (low, medium, high)
        - concerns: Any concerns or issues identified
        - recommendation: Your recommendation

        This agent ALWAYS escalates for human approval.
        """;

    public ReviewAgentNode(AdkConfig adkConfig, AdkSessionManager sessionManager) {

        super(adkConfig, sessionManager);
    }

    @PostConstruct
    public void init() {

        log.info("Initializing ReviewAgentNode...");

        this.agent = LlmAgent.builder()
            .name("review_agent")
            .description("Human-in-the-loop approval step")
            .model(adkConfig.getDefaultModel())
            .instruction(REVIEW_INSTRUCTION)
            .build();

        log.info("ReviewAgentNode initialized");
    }

    @Override
    public String getName() {

        return "review_agent";
    }

    @Override
    public String getDescription() {

        return "Human-in-the-loop approval step";
    }

    @Override
    protected String buildPrompt(AgenticaState state, Map<String, Object> config) {

        Event event = state.event()
            .orElseThrow(() -> new IllegalStateException("Event not found in state"));

        String payloadJson = JsonUtils.toPrettyJson(event.payload())
            .orElse("{}");

        String nodeOutputsJson = JsonUtils.toPrettyJson(state.nodeOutputs())
            .orElse("{}");

        String planJson = state.currentPlan()
            .map(plan -> JsonUtils.toPrettyJson(plan).orElse("{}"))
            .orElse("{}");

        return String.format("""
            Prepare a review summary for human approval:

            Event Information:
            - Event ID: %s
            - Event Type: %s
            - Source: %s

            Event Payload:
            %s

            Workflow Plan:
            %s

            Node Outputs So Far:
            %s

            Summarize the situation and prepare for human review.
            Identify any risks and provide your recommendation.
            """,
            event.id(),
            event.eventType(),
            event.source(),
            payloadJson,
            planJson,
            nodeOutputsJson
        );
    }

    @Override
    protected AgentNodeResult processResult(String result, AgenticaState state) {

        if (adkConfig.isAutoApproveReview()) {

            log.info("Review agent completed, auto-approve enabled - proceeding without human approval");

            Map<String, Object> output = Map.of(
                "approved", true,
                "reviewSummary", result,
                "approvalMode", "auto"
            );

            return AgentNodeResult.success(output);
        }

        log.info("Review agent completed, escalating for human approval");

        return AgentNodeResult.escalate(
            "APPROVAL_NEEDED",
            "Human approval required. Review summary: " + result
        );
    }

}
