CREATE TABLE IF NOT EXISTS comment (
    id VARCHAR(36) PRIMARY KEY,
    message TEXT NOT NULL,
    incident_id BIGINT NOT NULL REFERENCES incident(id),
    user_id VARCHAR(255) NOT NULL REFERENCES app_user(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_comment_incident_id ON comment(incident_id);
CREATE INDEX IF NOT EXISTS idx_comment_user_id ON comment(user_id);

COMMENT ON TABLE comment IS 'Comment connected to incident';
COMMENT ON COLUMN comment.id IS 'Unike comment ID (UUID)';
COMMENT ON COLUMN comment.message IS 'Comment text';
COMMENT ON COLUMN comment.incident_id IS 'Incident this comment belongs to';
COMMENT ON COLUMN comment.user_id IS 'User who wrote the comment';
COMMENT ON COLUMN comment.created_at IS 'When the comment was created';