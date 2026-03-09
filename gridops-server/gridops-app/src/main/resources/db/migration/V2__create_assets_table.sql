CREATE TABLE assets (
    id             BIGSERIAL       PRIMARY KEY,
    asset_tag      VARCHAR(50)     NOT NULL UNIQUE,
    name           VARCHAR(100)    NOT NULL,
    asset_type     VARCHAR(30)     NOT NULL CHECK (asset_type IN ('SUBSTATION', 'TRANSFORMER', 'LINE_SEGMENT', 'SWITCH', 'METER')),
    status         VARCHAR(20)     NOT NULL DEFAULT 'OPERATIONAL' CHECK (status IN ('OPERATIONAL', 'DEGRADED', 'OFFLINE', 'MAINTENANCE')),
    location       VARCHAR(255),
    latitude       DECIMAL(10,7),
    longitude      DECIMAL(10,7),
    installed_date DATE,
    created_by     BIGINT          NOT NULL REFERENCES users(id),
    created_at     TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_assets_type ON assets (asset_type);
CREATE INDEX idx_assets_status ON assets (status);
CREATE INDEX idx_assets_created_by ON assets (created_by);
