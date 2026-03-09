CREATE TABLE users (
    id            BIGSERIAL       PRIMARY KEY,
    username      VARCHAR(50)     NOT NULL UNIQUE,
    email         VARCHAR(100)    NOT NULL UNIQUE,
    password_hash VARCHAR(72)     NOT NULL,
    role          VARCHAR(20)     NOT NULL CHECK (role IN ('ADMIN', 'ENGINEER', 'OPERATOR')),
    active        BOOLEAN         NOT NULL DEFAULT true,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_active ON users (active);
