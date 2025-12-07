-- Ticket classifications table: AI classification results for support tickets
-- Stores classification decisions for Repharma support ticket routing

CREATE TABLE ticket_classifications (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       VARCHAR(64) NOT NULL,
    event_id        UUID NOT NULL REFERENCES events(id),
    category        VARCHAR(64) NOT NULL,
    subcategory     VARCHAR(64),
    confidence      DECIMAL(5, 4) NOT NULL,
    reasoning       TEXT,
    suggested_action VARCHAR(64),
    route_to        VARCHAR(128),
    auto_response   BOOLEAN DEFAULT FALSE,
    response_template VARCHAR(128),
    metadata        JSONB,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes for classification queries
CREATE INDEX idx_ticket_classifications_tenant_id ON ticket_classifications(tenant_id);
CREATE INDEX idx_ticket_classifications_event_id ON ticket_classifications(event_id);
CREATE INDEX idx_ticket_classifications_category ON ticket_classifications(category);
CREATE INDEX idx_ticket_classifications_route_to ON ticket_classifications(route_to) WHERE route_to IS NOT NULL;

COMMENT ON TABLE ticket_classifications IS 'AI classification results for support ticket routing';
COMMENT ON COLUMN ticket_classifications.category IS 'Classification category: ORDER_STATUS, PRODUCT_INFO, RETURNS_REFUNDS, COMPLAINTS, PAYMENT_ISSUES, PRESCRIPTION_QUESTIONS';
COMMENT ON COLUMN ticket_classifications.confidence IS 'Classification confidence score (0.0000 to 1.0000)';
COMMENT ON COLUMN ticket_classifications.suggested_action IS 'Suggested action: AUTO_RESPOND, ROUTE_TO_TEAM, ESCALATE';
COMMENT ON COLUMN ticket_classifications.route_to IS 'Target team/person for routing (e.g., support_team, finance_team, pharmacist, manager)';
COMMENT ON COLUMN ticket_classifications.auto_response IS 'Whether an automatic response can be sent';
COMMENT ON COLUMN ticket_classifications.response_template IS 'Template identifier for auto-response';
