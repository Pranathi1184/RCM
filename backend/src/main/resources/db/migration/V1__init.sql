CREATE TABLE regulatory_change (
    id                  BIGSERIAL PRIMARY KEY,
    title               VARCHAR(255)        NOT NULL,
    description         TEXT,
    regulatory_body     VARCHAR(255)        NOT NULL,
    category            VARCHAR(100)        NOT NULL,
    status              VARCHAR(50)         NOT NULL DEFAULT 'PENDING',
    priority            VARCHAR(50)         NOT NULL DEFAULT 'MEDIUM',
    impact_score        NUMERIC(3, 1),
    effective_date      DATE,
    deadline            DATE,
    assigned_to         VARCHAR(255),
    ai_description      TEXT,
    ai_recommendations  TEXT,
    file_path           VARCHAR(500),
    is_deleted          BOOLEAN             NOT NULL DEFAULT FALSE,
    created_by          VARCHAR(255),
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

--APIs will filter by status, category, deadlines
-- Searched/filtered frequently
CREATE INDEX idx_regulatory_change_status       ON regulatory_change (status);
CREATE INDEX idx_regulatory_change_category     ON regulatory_change (category);
CREATE INDEX idx_regulatory_change_deadline     ON regulatory_change (deadline);
CREATE INDEX idx_regulatory_change_effective_date ON regulatory_change (effective_date);
CREATE INDEX idx_regulatory_change_is_deleted   ON regulatory_change (is_deleted);

-- For soft-delete + status combo queries
CREATE INDEX idx_regulatory_change_status_deleted
    ON regulatory_change (status, is_deleted);

    -- Auto-update updated_at on every row change
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_regulatory_change_updated_at
    BEFORE UPDATE ON regulatory_change
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();