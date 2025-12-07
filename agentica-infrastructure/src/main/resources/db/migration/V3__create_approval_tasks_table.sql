-- Approval tasks table: Human-in-the-loop approval queue
-- Trello-style board for reviewing and approving AI-generated workflows

CREATE TABLE approval_tasks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(64) NOT NULL,
    workflow_id     UUID NOT NULL REFERENCES workflows(id),
    title           VARCHAR(256) NOT NULL,
    description     TEXT,
    priority        INTEGER DEFAULT 0,
    category        VARCHAR(64),
    assigned_to     VARCHAR(128),
    status          VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    due_at          TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    reviewed_at     TIMESTAMP WITH TIME ZONE,
    reviewed_by     VARCHAR(128),
    review_notes    TEXT,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes for approval board queries
CREATE INDEX idx_approval_tasks_tenant_id ON approval_tasks(tenant_id);
CREATE INDEX idx_approval_tasks_workflow_id ON approval_tasks(workflow_id);
CREATE INDEX idx_approval_tasks_status ON approval_tasks(status);
CREATE INDEX idx_approval_tasks_tenant_status ON approval_tasks(tenant_id, status);
CREATE INDEX idx_approval_tasks_assigned_to ON approval_tasks(assigned_to) WHERE assigned_to IS NOT NULL;
CREATE INDEX idx_approval_tasks_priority ON approval_tasks(priority DESC);
CREATE INDEX idx_approval_tasks_category ON approval_tasks(category) WHERE category IS NOT NULL;

COMMENT ON TABLE approval_tasks IS 'Human-in-the-loop approval queue for AI-generated workflows';
COMMENT ON COLUMN approval_tasks.status IS 'Task status: PENDING, IN_REVIEW, APPROVED, REJECTED';
COMMENT ON COLUMN approval_tasks.priority IS 'Priority level for sorting (higher = more urgent)';
COMMENT ON COLUMN approval_tasks.category IS 'Category for filtering (e.g., support, finance, marketing)';
