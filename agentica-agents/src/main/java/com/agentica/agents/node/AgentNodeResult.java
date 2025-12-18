package com.agentica.agents.node;

import lombok.Builder;

/**
 * Result from executing an agent node in a workflow.
 * Contains the output and optional escalation information.
 */
@Builder(toBuilder = true)
public record AgentNodeResult(

    /**
     * The output from the agent execution.
     * May be null if execution failed or escalated.
     */
    Object output,

    /**
     * Whether the agent needs to escalate to the orchestrator.
     */
    boolean needsEscalation,

    /**
     * The reason for escalation (e.g., EXCEPTION, UNCERTAIN, APPROVAL_NEEDED).
     */
    String escalationReason,

    /**
     * Additional context about the escalation.
     */
    String escalationContext

) {

    /**
     * Creates a successful result with output.
     *
     * @param output the agent output
     * @return a successful AgentNodeResult
     */
    public static AgentNodeResult success(Object output) {

        return new AgentNodeResult(output, false, null, null);
    }

    /**
     * Creates an escalation result.
     *
     * @param reason the escalation reason
     * @param context additional context
     * @return an escalation AgentNodeResult
     */
    public static AgentNodeResult escalate(String reason, String context) {

        return new AgentNodeResult(null, true, reason, context);
    }

    /**
     * Creates an escalation result from an exception.
     *
     * @param exception the exception that occurred
     * @return an escalation AgentNodeResult
     */
    public static AgentNodeResult fromException(Exception exception) {

        return new AgentNodeResult(
            null,
            true,
            "EXCEPTION",
            exception.getMessage()
        );
    }

}
