package com.agentica.api.controller;

import static com.agentica.common.constants.AgenticaConstants.TENANT_ID_HEADER;

import com.agentica.api.dto.request.ApprovalRequest;
import com.agentica.api.dto.response.WorkflowResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
@Tag(name = "Approvals", description = "Workflow approval endpoints")
public class ApprovalController {

  @GetMapping("/pending")
  @Operation(summary = "List pending approvals", description = "Returns all workflows awaiting approval")
  public ResponseEntity<List<WorkflowResponse>> listPendingApprovals(
      @RequestHeader(TENANT_ID_HEADER) final String tenantId) {

    log.info("Listing pending approvals for tenant: {}", tenantId);

    return ResponseEntity.ok(List.of());
  }

  @PostMapping("/{workflowId}/approve")
  @Operation(summary = "Approve workflow", description = "Approves a workflow for execution")
  public ResponseEntity<WorkflowResponse> approveWorkflow(
      @RequestHeader(TENANT_ID_HEADER) final String tenantId,
      @PathVariable final String workflowId,
      @Valid @RequestBody final ApprovalRequest request) {

    log.info("Approving workflow: {} for tenant: {} by user: {}",
        workflowId, tenantId, request.approvedBy());

    return ResponseEntity.notFound().build();
  }

  @PostMapping("/{workflowId}/reject")
  @Operation(summary = "Reject workflow", description = "Rejects a workflow")
  public ResponseEntity<WorkflowResponse> rejectWorkflow(
      @RequestHeader(TENANT_ID_HEADER) final String tenantId,
      @PathVariable final String workflowId,
      @Valid @RequestBody final ApprovalRequest request) {

    log.info("Rejecting workflow: {} for tenant: {} by user: {}",
        workflowId, tenantId, request.approvedBy());

    return ResponseEntity.notFound().build();
  }
}
