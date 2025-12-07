-- Events table: Core event ingestion storage
-- All external events (webhooks from WhatsApp, Viber, Facebook, Hookdeck) are stored here

CREATE TABLE events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(64) NOT NULL,
    event_type      VARCHAR(128) NOT NULL,
    source          VARCHAR(128) NOT NULL,
    external_id     VARCHAR(256),
    payload         JSONB NOT NULL,
    status          VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    category        VARCHAR(64),
    priority        INTEGER DEFAULT 0,
    filter_reasoning TEXT,
    received_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    processed_at    TIMESTAMP WITH TIME ZONE,
    error_message   TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes for common queries
CREATE INDEX idx_events_tenant_id ON events(tenant_id);
CREATE INDEX idx_events_status ON events(status);
CREATE INDEX idx_events_tenant_status ON events(tenant_id, status);
CREATE INDEX idx_events_source ON events(source);
CREATE INDEX idx_events_event_type ON events(event_type);
CREATE INDEX idx_events_received_at ON events(received_at DESC);
CREATE INDEX idx_events_external_id ON events(external_id) WHERE external_id IS NOT NULL;

-- GIN index for JSONB payload queries
CREATE INDEX idx_events_payload ON events USING GIN(payload);

COMMENT ON TABLE events IS 'Stores all ingested events from external sources (WhatsApp, Viber, Facebook, webhooks)';
COMMENT ON COLUMN events.tenant_id IS 'Tenant identifier for multi-tenant isolation';
COMMENT ON COLUMN events.event_type IS 'Type of event (e.g., message.received, comment.created, payment.completed)';
COMMENT ON COLUMN events.source IS 'Source system (e.g., whatsapp, viber, facebook, hookdeck)';
COMMENT ON COLUMN events.external_id IS 'External system identifier for deduplication';
COMMENT ON COLUMN events.status IS 'Event processing status: PENDING, PROCESSING, FILTERED, ACTIONABLE, COMPLETED, FAILED';
COMMENT ON COLUMN events.category IS 'AI-classified category (e.g., order_status, product_info, complaint)';
COMMENT ON COLUMN events.priority IS 'Priority level (0=low, higher=more urgent)';
