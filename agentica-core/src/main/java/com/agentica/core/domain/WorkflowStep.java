package com.agentica.core.domain;

import com.agentica.core.enums.AgentType;
import lombok.Builder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * Represents a single step in a workflow execution plan.
 */
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record WorkflowStep(

        int stepNumber,

        AgentType agentType,

        String action,

        String description,

        Map<String, Object> parameters,

        boolean completed,

        String result,

        String errorMessage

) {

    /**
     * Returns a copy of this step marked as completed with a result.
     */
    public WorkflowStep markAsCompleted(String result) {
        return this.toBuilder()
                .completed(true)
                .result(result)
                .build();
    }

    /**
     * Returns a copy of this step marked as failed with an error message.
     */
    public WorkflowStep markAsFailed(String errorMessage) {
        return this.toBuilder()
                .completed(false)
                .errorMessage(errorMessage)
                .build();
    }

}
