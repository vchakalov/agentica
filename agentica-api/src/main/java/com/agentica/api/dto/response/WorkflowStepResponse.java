package com.agentica.api.dto.response;

import com.agentica.core.enums.AgentType;

import lombok.Builder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * Response DTO for workflow step data.
 */
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record WorkflowStepResponse(

        int stepNumber,

        AgentType agentType,

        String action,

        String description,

        Map<String, Object> parameters,

        boolean completed,

        String result,

        String errorMessage

) {}
