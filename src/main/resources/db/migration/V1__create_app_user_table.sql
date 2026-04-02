CREATE TABLE IF NOT EXISTS app_user (
                                        id VARCHAR(255) PRIMARY KEY,
                                        github_id VARCHAR(255) NOT NULL UNIQUE,
                                        github_login VARCHAR(255) NOT NULL UNIQUE,
                                        email VARCHAR(255) UNIQUE,
                                        name VARCHAR(255) NOT NULL,
                                        role VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                                        avatar_url VARCHAR(500),
                                        is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_app_user_email ON app_user(email);
CREATE INDEX IF NOT EXISTS idx_app_user_role ON app_user(role);
CREATE INDEX IF NOT EXISTS idx_app_user_github_id ON app_user(github_id);

COMMENT ON TABLE app_user IS 'Users logging in through GitHub OAuth2';
COMMENT ON COLUMN app_user.role IS 'Role: PENDING, RESIDENT, HANDLER, ADMIN';