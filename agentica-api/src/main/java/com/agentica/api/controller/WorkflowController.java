package com.agentica.api.controller;

import com.agentica.api.dto.response.WorkflowResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.agentica.common.constants.AgenticaConstants.TENANT_ID_HEADER;

/**
 * Controller for workflow management.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
@Tag(name = "Workflows", description = "Workflow management endpoints")
public class WorkflowController {

    @GetMapping
    @Operation(summary = "List workflows", description = "Returns all workflows for the tenant")
    public ResponseEntity<List<WorkflowResponse>> listWorkflows(
            @RequestHeader(TENANT_ID_HEADER) String tenantId) {

        log.info("Listing workflows for tenant: {}", tenantId);

        // TODO: Implement actual workflow listing
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{workflowId}")
    @Operation(summary = "Get workflow", description = "Returns a specific workflow by ID")
    public ResponseEntity<WorkflowResponse> getWorkflow(
            @RequestHeader(TENANT_ID_HEADER) String tenantId,
            @PathVariable String workflowId) {

        log.info("Getting workflow: {} for tenant: {}", workflowId, tenantId);

        // TODO: Implement actual workflow retrieval
        return ResponseEntity.notFound().build();
    }

}
