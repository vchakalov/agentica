package com.agentica.infrastructure.persistence.repository;

import com.agentica.core.domain.Workflow;
import com.agentica.core.enums.WorkflowStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Workflow persistence.
 * Provides abstraction over storage mechanism (in-memory, database, etc.)
 */
public interface WorkflowRepository {

    Workflow save(Workflow workflow);

    Optional<Workflow> findById(String id);

    Optional<Workflow> findByEventId(String eventId);

    List<Workflow> findByTenantId(String tenantId);

    List<Workflow> findByTenantIdAndStatus(String tenantId, WorkflowStatus status);

    List<Workflow> findByStatus(WorkflowStatus status);

    List<Workflow> findPendingApproval(String tenantId, int limit);

    long countByTenantIdAndStatus(String tenantId, WorkflowStatus status);

    void deleteById(String id);

    void deleteByTenantId(String tenantId);

}
