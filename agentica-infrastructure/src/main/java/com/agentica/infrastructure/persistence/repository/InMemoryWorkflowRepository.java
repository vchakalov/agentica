package com.agentica.infrastructure.persistence.repository;

import com.agentica.core.domain.Workflow;
import com.agentica.core.enums.WorkflowStatus;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of WorkflowRepository.
 * Suitable for development and testing. Replace with JPA implementation for production.
 */
@Slf4j
@Repository
public class InMemoryWorkflowRepository implements WorkflowRepository {

    private final Map<String, Workflow> workflows = new ConcurrentHashMap<>();

    @Override
    public Workflow save(Workflow workflow) {

        String id = workflow.id();

        if (id == null || id.isBlank()) {

            id = UUID.randomUUID().toString();

            workflow = workflow.toBuilder().id(id).build();
        }

        workflows.put(id, workflow);

        log.debug("Saved workflow, id: {}, eventId: {}, status: {}",
            id, workflow.eventId(), workflow.status());

        return workflow;
    }

    @Override
    public Optional<Workflow> findById(String id) {

        return Optional.ofNullable(workflows.get(id));
    }

    @Override
    public Optional<Workflow> findByEventId(String eventId) {

        return workflows.values().stream()
            .filter(w -> eventId.equals(w.eventId()))
            .findFirst();
    }

    @Override
    public List<Workflow> findByTenantId(String tenantId) {

        return workflows.values().stream()
            .filter(w -> tenantId.equals(w.tenantId()))
            .sorted(Comparator.comparing(Workflow::createdAt).reversed())
            .toList();
    }

    @Override
    public List<Workflow> findByTenantIdAndStatus(String tenantId, WorkflowStatus status) {

        return workflows.values().stream()
            .filter(w -> tenantId.equals(w.tenantId()))
            .filter(w -> status.equals(w.status()))
            .sorted(Comparator.comparing(Workflow::createdAt).reversed())
            .toList();
    }

    @Override
    public List<Workflow> findByStatus(WorkflowStatus status) {

        return workflows.values().stream()
            .filter(w -> status.equals(w.status()))
            .sorted(Comparator.comparing(Workflow::createdAt).reversed())
            .toList();
    }

    @Override
    public List<Workflow> findPendingApproval(String tenantId, int limit) {

        return workflows.values().stream()
            .filter(w -> tenantId.equals(w.tenantId()))
            .filter(w -> WorkflowStatus.AWAITING_APPROVAL.equals(w.status()))
            .sorted(Comparator.comparing(Workflow::createdAt))
            .limit(limit)
            .toList();
    }

    @Override
    public long countByTenantIdAndStatus(String tenantId, WorkflowStatus status) {

        return workflows.values().stream()
            .filter(w -> tenantId.equals(w.tenantId()))
            .filter(w -> status.equals(w.status()))
            .count();
    }

    @Override
    public void deleteById(String id) {

        workflows.remove(id);

        log.debug("Deleted workflow, id: {}", id);
    }

    @Override
    public void deleteByTenantId(String tenantId) {

        List<String> idsToRemove = workflows.values().stream()
            .filter(w -> tenantId.equals(w.tenantId()))
            .map(Workflow::id)
            .toList();

        idsToRemove.forEach(workflows::remove);

        log.debug("Deleted all workflows for tenant, tenantId: {}, count: {}",
            tenantId, idsToRemove.size());
    }

}
