package com.agentica.api.controller;

import com.agentica.api.dto.response.DebugStateResponse;
import com.agentica.api.dto.response.DebugStateResponse.EventInfo;
import com.agentica.api.dto.response.DebugStateResponse.ExecutionInfo;
import com.agentica.api.dto.response.DebugStateResponse.ExecutionSummary;
import com.agentica.api.dto.response.DebugStateResponse.GraphEdge;
import com.agentica.api.dto.response.DebugStateResponse.GraphInfo;
import com.agentica.api.dto.response.DebugStateResponse.GraphNode;
import com.agentica.api.dto.response.DebugStateResponse.NodeExecutionInfo;
import com.agentica.api.dto.response.DebugStateResponse.ReplanInfoDto;
import com.agentica.core.workflow.WorkflowEdge;
import com.agentica.core.workflow.WorkflowNode;
import com.agentica.core.workflow.WorkflowPlan;
import com.agentica.workflows.debug.ExecutionSnapshot;
import com.agentica.workflows.debug.ExecutionStore;
import com.agentica.workflows.debug.NodeExecution;
import com.agentica.workflows.registry.WorkflowRegistry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for the debugging dashboard.
 * Provides real-time execution state for the custom debug UI.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/debug")
@RequiredArgsConstructor
@Tag(name = "Debug", description = "Debugging dashboard endpoints")
public class DebugController {

    private final ExecutionStore executionStore;

    private final WorkflowRegistry workflowRegistry;

    @GetMapping("/state")
    @Operation(
        summary = "Get current debug state",
        description = "Returns the current execution state for the debugging dashboard"
    )
    public ResponseEntity<DebugStateResponse> getCurrentState() {

        log.debug("Getting current debug state");

        Optional<ExecutionSnapshot> currentOpt = executionStore.getCurrent();

        ExecutionSnapshot current = currentOpt.orElse(null);

        WorkflowPlan plan = current != null ? current.plan()
            : workflowRegistry.getLatestPlan().orElse(null);

        List<ExecutionSnapshot> history = executionStore.getHistory();

        DebugStateResponse response = DebugStateResponse.builder()
            .hasActiveExecution(current != null)
            .currentExecution(ExecutionInfo.from(current))
            .triggerEvent(current != null ? EventInfo.from(current.triggerEvent()) : null)
            .graph(buildGraphInfo(plan, current))
            .activeNodeId(current != null ? current.activeNodeId() : null)
            .timeline(buildTimeline(current))
            .replans(buildReplans(current))
            .recentHistory(buildHistorySummary(history, 10))
            .timestamp(Instant.now())
            .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @Operation(
        summary = "Get execution history",
        description = "Returns a list of recent workflow executions"
    )
    public ResponseEntity<List<ExecutionSummary>> getHistory(
        @RequestParam(defaultValue = "50") int limit) {

        log.debug("Getting execution history, limit: {}", limit);

        List<ExecutionSnapshot> history = executionStore.getHistory();

        List<ExecutionSummary> summaries = buildHistorySummary(history, limit);

        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/{eventId}")
    @Operation(
        summary = "Get execution details",
        description = "Returns full execution details for a specific event"
    )
    public ResponseEntity<DebugStateResponse> getExecution(@PathVariable String eventId) {

        log.debug("Getting execution details, eventId: {}", eventId);

        Optional<ExecutionSnapshot> snapshotOpt = executionStore.findByEventId(eventId);

        if (snapshotOpt.isEmpty()) {

            return ResponseEntity.notFound().build();
        }

        ExecutionSnapshot snapshot = snapshotOpt.get();

        DebugStateResponse response = DebugStateResponse.builder()
            .hasActiveExecution(snapshot.state() == ExecutionSnapshot.ExecutionState.RUNNING)
            .currentExecution(ExecutionInfo.from(snapshot))
            .triggerEvent(EventInfo.from(snapshot.triggerEvent()))
            .graph(buildGraphInfo(snapshot.plan(), snapshot))
            .activeNodeId(snapshot.activeNodeId())
            .timeline(buildTimeline(snapshot))
            .replans(buildReplans(snapshot))
            .recentHistory(List.of())
            .timestamp(Instant.now())
            .build();

        return ResponseEntity.ok(response);
    }

    private GraphInfo buildGraphInfo(WorkflowPlan plan, ExecutionSnapshot snapshot) {

        if (plan == null) {

            return GraphInfo.builder()
                .nodes(List.of())
                .edges(List.of())
                .build();
        }

        Map<String, NodeExecution> nodeExecutions = snapshot != null
            ? snapshot.nodeExecutions()
            : Map.of();

        String activeNodeId = snapshot != null ? snapshot.activeNodeId() : null;

        List<GraphNode> nodes = new ArrayList<>();

        // Add orchestrator as the first node (it designed this workflow)
        String orchestratorStatus = determineOrchestratorStatus(snapshot);

        nodes.add(GraphNode.builder()
            .id("__orchestrator__")
            .agentType(com.agentica.core.enums.AgentType.ORCHESTRATOR)
            .description("Plans workflow based on event")
            .status(orchestratorStatus)
            .build());

        // Add all workflow nodes
        for (WorkflowNode node : plan.nodes()) {

            nodes.add(GraphNode.builder()
                .id(node.id())
                .agentType(node.agentType())
                .description(node.description())
                .status(determineNodeStatus(node.id(), nodeExecutions, activeNodeId))
                .build());
        }

        List<GraphEdge> edges = new ArrayList<>();

        // Add edge from orchestrator to entry point
        edges.add(GraphEdge.builder()
            .from("__orchestrator__")
            .to(plan.entryPoint())
            .type("DIRECT")
            .label("delegates")
            .build());

        // Add all workflow edges (except START edges since orchestrator handles entry point)
        for (WorkflowEdge edge : plan.edges()) {

            // Skip START edges - orchestrator already connects to entry point
            if ("START".equals(edge.from())) {

                continue;
            }

            edges.add(GraphEdge.builder()
                .from(edge.from())
                .to(edge.to())
                .type(edge.type() != null ? edge.type().name() : "DIRECT")
                .label(buildEdgeLabel(edge))
                .build());
        }

        return GraphInfo.builder()
            .nodes(nodes)
            .edges(edges)
            .build();
    }

    private String determineOrchestratorStatus(ExecutionSnapshot snapshot) {

        if (snapshot == null) {

            return "PENDING";
        }

        // If we have a plan, orchestrator has completed its planning
        if (snapshot.plan() != null) {

            // If there are replans, show as "REPLANNED" to indicate it was invoked again
            if (snapshot.replanCount() > 0) {

                return "REPLANNED";
            }

            return "COMPLETED";
        }

        return "PENDING";
    }

    private String determineNodeStatus(String nodeId, Map<String, NodeExecution> executions,
        String activeNodeId) {

        if (nodeId.equals(activeNodeId)) {

            return "RUNNING";
        }

        NodeExecution execution = executions.get(nodeId);

        if (execution == null) {

            return "PENDING";
        }

        return execution.status() != null ? execution.status().name() : "PENDING";
    }

    private String buildEdgeLabel(WorkflowEdge edge) {

        if (edge.condition() == null) {

            return null;
        }

        return edge.condition().stateKey();
    }

    private List<NodeExecutionInfo> buildTimeline(ExecutionSnapshot snapshot) {

        if (snapshot == null || snapshot.executionOrder() == null) {

            return List.of();
        }

        List<NodeExecutionInfo> timeline = new ArrayList<>();

        for (String nodeId : snapshot.executionOrder()) {

            NodeExecution execution = snapshot.nodeExecutions().get(nodeId);

            if (execution != null) {

                timeline.add(NodeExecutionInfo.from(execution));
            }
        }

        return timeline;
    }

    private List<ExecutionSummary> buildHistorySummary(List<ExecutionSnapshot> history, int limit) {

        return history.stream()
            .limit(limit)
            .map(ExecutionSummary::from)
            .toList();
    }

    private List<ReplanInfoDto> buildReplans(ExecutionSnapshot snapshot) {

        if (snapshot == null || snapshot.replanHistory() == null) {

            return List.of();
        }

        return snapshot.replanHistory().stream()
            .map(ReplanInfoDto::from)
            .toList();
    }

}
