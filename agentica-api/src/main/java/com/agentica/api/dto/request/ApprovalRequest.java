package com.agentica.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Request DTO for workflow approval/rejection.
 */
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApprovalRequest(

        @NotBlank
        String approvedBy,

        String comment,

        String reason

) {}
