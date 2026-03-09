CREATE TABLE incident_history (
    id            BIGSERIAL       PRIMARY KEY,
    incident_id   BIGINT          NOT NULL REFERENCES incidents(id),
    changed_by    BIGINT          NOT NULL REFERENCES users(id),
    field_changed VARCHAR(50)     NOT NULL,
    old_value     TEXT,
    new_value     TEXT            NOT NULL,
    change_note   TEXT,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_history_incident_id ON incident_history (incident_id);
CREATE INDEX idx_history_changed_by ON incident_history (changed_by);
CREATE INDEX idx_history_created_at ON incident_history (created_at DESC);
