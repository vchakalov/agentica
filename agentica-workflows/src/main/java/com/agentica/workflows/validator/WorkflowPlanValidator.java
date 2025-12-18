package com.agentica.workflows.validator;

import com.agentica.core.enums.AgentType;
import com.agentica.core.workflow.EdgeType;
import com.agentica.core.workflow.WorkflowEdge;
import com.agentica.core.workflow.WorkflowNode;
import com.agentica.core.workflow.WorkflowPlan;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates WorkflowPlan before execution.
 * Ensures the plan is structurally sound and executable.
 */
@Slf4j
@Component
public class WorkflowPlanValidator {

    private static final String START_NODE = "START";

    private static final String END_NODE = "END";

    private static final Set<AgentType> ALLOWED_AGENT_TYPES = Set.of(
        AgentType.MARKETING,
        AgentType.FACEBOOK,
        AgentType.SUPPORT,
        AgentType.REVIEW,
        AgentType.TRANSLATION,
        AgentType.FINANCE
    );

    /**
     * Validates a workflow plan.
     *
     * @param plan the plan to validate
     * @throws WorkflowValidationException if the plan is invalid
     */
    public void validate(WorkflowPlan plan) {

        log.debug("Validating workflow plan, planId: {}", plan.workflowId());

        List<String> errors = new ArrayList<>();

        validateRequiredFields(plan, errors);

        if (!errors.isEmpty()) {

            throw new WorkflowValidationException(plan.workflowId(), errors);
        }

        Set<String> nodeIds = plan.nodes().stream()
            .map(WorkflowNode::id)
            .collect(Collectors.toSet());

        validateEntryPoint(plan, nodeIds, errors);

        validateNodes(plan, errors);

        validateEdges(plan, nodeIds, errors);

        validateConnectivity(plan, nodeIds, errors);

        if (!errors.isEmpty()) {

            log.warn("Workflow plan validation failed, planId: {}, errors: {}",
                plan.workflowId(), errors);

            throw new WorkflowValidationException(plan.workflowId(), errors);
        }

        log.debug("Workflow plan validated successfully, planId: {}", plan.workflowId());
    }

    private void validateRequiredFields(WorkflowPlan plan, List<String> errors) {

        if (plan.workflowId() == null || plan.workflowId().isBlank()) {

            errors.add("workflowId is required");
        }

        if (plan.workflowName() == null || plan.workflowName().isBlank()) {

            errors.add("workflowName is required");
        }

        if (plan.nodes() == null || plan.nodes().isEmpty()) {

            errors.add("At least one node is required");
        }

        if (plan.edges() == null || plan.edges().isEmpty()) {

            errors.add("At least one edge is required");
        }

        if (plan.entryPoint() == null || plan.entryPoint().isBlank()) {

            errors.add("entryPoint is required");
        }
    }

    private void validateEntryPoint(WorkflowPlan plan, Set<String> nodeIds, List<String> errors) {

        String entryPoint = plan.entryPoint();

        if (!nodeIds.contains(entryPoint)) {

            errors.add("Entry point '" + entryPoint + "' does not reference a valid node");
        }

        boolean hasStartEdge = plan.edges().stream()
            .anyMatch(edge -> START_NODE.equalsIgnoreCase(edge.from()) &&
                entryPoint.equals(edge.to()));

        if (!hasStartEdge) {

            errors.add("No edge from START to entry point '" + entryPoint + "'");
        }
    }

    private void validateNodes(WorkflowPlan plan, List<String> errors) {

        Set<String> seenIds = new HashSet<>();

        for (WorkflowNode node : plan.nodes()) {

            if (node.id() == null || node.id().isBlank()) {

                errors.add("Node id is required");
                continue;
            }

            if (seenIds.contains(node.id())) {

                errors.add("Duplicate node id: " + node.id());
            }

            seenIds.add(node.id());

            if (node.agentType() == null) {

                errors.add("Node '" + node.id() + "' missing agentType");

            } else if (!ALLOWED_AGENT_TYPES.contains(node.agentType())) {

                errors.add("Node '" + node.id() + "' has invalid agentType: " + node.agentType());
            }

            if (node.description() == null || node.description().isBlank()) {

                errors.add("Node '" + node.id() + "' missing description");
            }
        }
    }

    private void validateEdges(WorkflowPlan plan, Set<String> nodeIds, List<String> errors) {

        // CRITICAL: LangGraph4j allows only ONE outgoing edge per node
        // Track which nodes already have outgoing edges defined
        Set<String> nodesWithOutgoingEdge = new HashSet<>();

        for (WorkflowEdge edge : plan.edges()) {

            if (edge.from() == null || edge.from().isBlank()) {

                errors.add("Edge 'from' is required");
                continue;
            }

            if (edge.to() == null || edge.to().isBlank()) {

                errors.add("Edge 'to' is required");
                continue;
            }

            // Check for duplicate outgoing edges from the same node
            // LangGraph4j will throw: "conditional edge from 'X' already exist!"
            String fromNormalized = edge.from().toUpperCase();

            if (!START_NODE.equals(fromNormalized)) {

                if (nodesWithOutgoingEdge.contains(edge.from())) {

                    errors.add("LANGGRAPH4J CONSTRAINT VIOLATION: Node '" + edge.from() +
                        "' has multiple outgoing edges. Each node can only have ONE outgoing edge. " +
                        "If you need multiple routing targets, use a single CONDITIONAL edge with " +
                        "all routes in its condition.routes array.");

                } else {

                    nodesWithOutgoingEdge.add(edge.from());
                }
            }

            if (!isValidNodeReference(edge.from(), nodeIds)) {

                errors.add("Edge 'from' references unknown node: " + edge.from());
            }

            if (!isValidNodeReference(edge.to(), nodeIds)) {

                errors.add("Edge 'to' references unknown node: " + edge.to());
            }

            if (edge.type() == null) {

                errors.add("Edge from '" + edge.from() + "' to '" + edge.to() + "' missing type");
            }

            if (edge.type() == EdgeType.CONDITIONAL) {

                validateConditionalEdge(edge, nodeIds, errors);
            }
        }
    }

    private void validateConditionalEdge(WorkflowEdge edge, Set<String> nodeIds, List<String> errors) {

        if (edge.condition() == null) {

            errors.add("Conditional edge from '" + edge.from() + "' missing condition config");
            return;
        }

        if (edge.condition().stateKey() == null || edge.condition().stateKey().isBlank()) {

            errors.add("Conditional edge from '" + edge.from() + "' missing stateKey");
        }

        if (edge.condition().routes() != null) {

            for (var route : edge.condition().routes()) {

                String targetNode = route.target();

                if (!isValidNodeReference(targetNode, nodeIds)) {

                    errors.add("Conditional route '" + route.value() + "' references unknown node: " + targetNode);
                }
            }
        }
    }

    private void validateConnectivity(WorkflowPlan plan, Set<String> nodeIds, List<String> errors) {

        Set<String> reachable = new HashSet<>();

        reachable.add(plan.entryPoint());

        boolean changed = true;

        while (changed) {

            changed = false;

            for (WorkflowEdge edge : plan.edges()) {

                if (reachable.contains(edge.from()) || START_NODE.equalsIgnoreCase(edge.from())) {

                    String target = edge.to();

                    if (!END_NODE.equalsIgnoreCase(target) && nodeIds.contains(target)) {

                        if (reachable.add(target)) {

                            changed = true;
                        }
                    }

                    if (edge.type() == EdgeType.CONDITIONAL && edge.condition() != null &&
                        edge.condition().routes() != null) {

                        for (var route : edge.condition().routes()) {

                            String routeTarget = route.target();

                            if (!END_NODE.equalsIgnoreCase(routeTarget) && nodeIds.contains(routeTarget)) {

                                if (reachable.add(routeTarget)) {

                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        Set<String> orphans = new HashSet<>(nodeIds);

        orphans.removeAll(reachable);

        if (!orphans.isEmpty()) {

            // Provide helpful diagnostic information
            log.debug("Reachable nodes: {}", reachable);
            log.debug("All node IDs: {}", nodeIds);
            log.debug("Orphan nodes: {}", orphans);

            errors.add("Orphan nodes not reachable from entry point '" + plan.entryPoint() +
                "': " + orphans + ". Reachable nodes: " + reachable +
                ". Check that your CONDITIONAL edge routes include all necessary targets.");
        }

        boolean hasEndEdge = plan.edges().stream()
            .anyMatch(edge -> END_NODE.equalsIgnoreCase(edge.to()));

        if (!hasEndEdge) {

            errors.add("No edge leading to END node");
        }
    }

    private boolean isValidNodeReference(String nodeRef, Set<String> nodeIds) {

        if (START_NODE.equalsIgnoreCase(nodeRef) || END_NODE.equalsIgnoreCase(nodeRef)) {

            return true;
        }

        return nodeIds.contains(nodeRef);
    }

}
