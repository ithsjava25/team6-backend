CREATE TABLE audit_log (
                           id BIGSERIAL PRIMARY KEY,
                           user_name VARCHAR(255),
                           action VARCHAR(255),
                           target_type VARCHAR(255),
                           target_id VARCHAR(255),
                           details TEXT,
                           created_at TIMESTAMP,
                           user_id VARCHAR(255)
);