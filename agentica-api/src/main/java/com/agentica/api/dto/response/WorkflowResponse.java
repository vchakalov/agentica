package com.agentica.api.dto.response;

import com.agentica.core.enums.WorkflowStatus;

import lombok.Builder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for workflow data.
 */
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record WorkflowResponse(

        String id,

        String eventId,

        String name,

        String description,

        WorkflowStatus status,

        List<WorkflowStepResponse> steps,

        int currentStepIndex,

        Instant createdAt,

        Instant approvedAt,

        String approvedBy,

        Instant completedAt,

        String errorMessage

) {}
