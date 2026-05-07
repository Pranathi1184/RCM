CREATE TABLE audit_log (
    id                  BIGSERIAL PRIMARY KEY,
    entity_type         VARCHAR(255) NOT NULL,
    entity_id           BIGINT NOT NULL,
    operation           VARCHAR(50) NOT NULL,
    old_value           JSONB,
    new_value           JSONB,
    modified_by         VARCHAR(255),
    modified_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Composite index for fast lookups of history for a specific entity
CREATE INDEX idx_audit_log_entity ON audit_log (entity_type, entity_id);

-- Index for searching history by date ranges
CREATE INDEX idx_audit_log_modified_at ON audit_log (modified_at);
