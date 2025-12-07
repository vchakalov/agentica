-- Workflows table: Stores AI-generated workflow executions
-- Each actionable event may trigger a workflow that requires approval

CREATE TABLE workflows (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(64) NOT NULL,
    event_id        UUID NOT NULL REFERENCES events(id),
    adk_session_id  VARCHAR(256),
    status          VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    name            VARCHAR(256),
    description     TEXT,
    steps           JSONB,
    current_step    INTEGER DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    approved_at     TIMESTAMP WITH TIME ZONE,
    approved_by     VARCHAR(128),
    rejected_at     TIMESTAMP WITH TIME ZONE,
    rejected_by     VARCHAR(128),
    rejection_reason TEXT,
    completed_at    TIMESTAMP WITH TIME ZONE,
    error_message   TEXT,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes for common queries
CREATE INDEX idx_workflows_tenant_id ON workflows(tenant_id);
CREATE INDEX idx_workflows_event_id ON workflows(event_id);
CREATE INDEX idx_workflows_status ON workflows(status);
CREATE INDEX idx_workflows_tenant_status ON workflows(tenant_id, status);
CREATE INDEX idx_workflows_created_at ON workflows(created_at DESC);

COMMENT ON TABLE workflows IS 'Stores AI-generated workflows pending approval or execution';
COMMENT ON COLUMN workflows.adk_session_id IS 'Google ADK session identifier for workflow context';
COMMENT ON COLUMN workflows.status IS 'Workflow status: DRAFT, AWAITING_APPROVAL, APPROVED, REJECTED, EXECUTING, COMPLETED, FAILED, CANCELLED';
COMMENT ON COLUMN workflows.steps IS 'JSON array of workflow steps with actions and parameters';
COMMENT ON COLUMN workflows.current_step IS 'Index of the currently executing step';
