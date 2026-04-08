CREATE TABLE IF NOT EXISTS activity_log (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    incident_id BIGINT NOT NULL REFERENCES incident(id),
    user_id VARCHAR(255) NOT NULL REFERENCES app_user(id)
);

CREATE INDEX IF NOT EXISTS idx_activity_log_incident_id ON activity_log(incident_id);
CREATE INDEX IF NOT EXISTS idx_activity_log_user_id ON activity_log(user_id);
CREATE INDEX IF NOT EXISTS idx_activity_log_created_at ON activity_log(created_at);

COMMENT ON TABLE activity_log IS 'Activity log entries for incident';
COMMENT ON COLUMN activity_log.action IS 'Type of activity, e.g CREATED, COMMENT_ADDED, STATUS_CHANGED';
COMMENT ON COLUMN activity_log.description IS 'Human-readable description of the activity';
COMMENT ON COLUMN activity_log.created_at IS 'When activity happened';
COMMENT ON COLUMN activity_log.incident_id IS 'Incident linked to this activity';
COMMENT ON COLUMN activity_log.user_id IS 'User who preformed the activity';
