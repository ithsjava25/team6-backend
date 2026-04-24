CREATE TABLE audit_log (
                           id BIGSERIAL PRIMARY KEY,
                           user_name VARCHAR(255),
                           action VARCHAR(255),
                           target_type VARCHAR(255),
                           target_id VARCHAR(255),
                           details TEXT,
                           created_at TIMESTAMP,
                           user_id VARCHAR(255),
                               CONSTRAINT fk_audit_log_user FOREIGN KEY (user_id)
                               REFERENCES app_user(id) ON DELETE SET NULL);
CREATE INDEX idx_audit_log_created_at ON audit_log (created_at DESC);
CREATE INDEX idx_audit_log_user_id    ON audit_log (user_id);
CREATE INDEX idx_audit_log_action     ON audit_log (action);
);