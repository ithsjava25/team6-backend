CREATE TABLE IF NOT EXISTS app_user (
    id VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    github_login VARCHAR(255) UNIQUE,
    role VARCHAR(50) NOT NULL DEFAULT 'RESIDENT',
    avatar_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_app_user_email ON app_user(email);
CREATE INDEX IF NOT EXISTS idx_app_user_role ON app_user(role);

COMMENT ON TABLE app_user IS 'Användare som loggar in via GitHub OAuth2';
COMMENT ON COLUMN app_user.role IS 'Roll: RESIDENT, HANDLER, ADMIN';