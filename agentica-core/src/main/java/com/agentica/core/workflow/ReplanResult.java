package com.agentica.core.workflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;

/**
 * Result from the orchestrator replan operation.
 * Contains both the decision and the LLM interaction details for debugging.
 */
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReplanResult(

    /**
     * The replan decision from the LLM.
     */
    ReplanDecision decision,

    /**
     * The prompt sent to the LLM.
     */
    String llmPrompt,

    /**
     * The raw response from the LLM.
     */
    String llmResponse

) {}
