CREATE SEQUENCE incident_number_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE incidents (
    id               BIGSERIAL       PRIMARY KEY,
    incident_number  VARCHAR(20)     NOT NULL UNIQUE,
    title            VARCHAR(200)    NOT NULL,
    description      TEXT,
    severity         VARCHAR(20)     NOT NULL DEFAULT 'MEDIUM' CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    status           VARCHAR(20)     NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')),
    asset_id         BIGINT          REFERENCES assets(id),
    reported_by      BIGINT          NOT NULL REFERENCES users(id),
    assigned_to      BIGINT          REFERENCES users(id),
    resolution_notes TEXT,
    created_at       TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at      TIMESTAMPTZ,
    closed_at        TIMESTAMPTZ
);

CREATE INDEX idx_incidents_status ON incidents (status);
CREATE INDEX idx_incidents_severity ON incidents (severity);
CREATE INDEX idx_incidents_asset_id ON incidents (asset_id);
CREATE INDEX idx_incidents_reported_by ON incidents (reported_by);
CREATE INDEX idx_incidents_assigned_to ON incidents (assigned_to);
CREATE INDEX idx_incidents_created_at ON incidents (created_at DESC);
