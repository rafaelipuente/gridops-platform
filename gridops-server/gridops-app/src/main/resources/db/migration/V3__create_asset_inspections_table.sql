CREATE TABLE asset_inspections (
    id              BIGSERIAL       PRIMARY KEY,
    asset_id        BIGINT          NOT NULL REFERENCES assets(id),
    inspected_by    BIGINT          NOT NULL REFERENCES users(id),
    inspection_date TIMESTAMPTZ     NOT NULL,
    notes           TEXT,
    condition       VARCHAR(20)     NOT NULL CHECK (condition IN ('GOOD', 'FAIR', 'POOR', 'CRITICAL')),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_inspections_asset_id ON asset_inspections (asset_id);
CREATE INDEX idx_inspections_inspected_by ON asset_inspections (inspected_by);
CREATE INDEX idx_inspections_date ON asset_inspections (inspection_date DESC);
