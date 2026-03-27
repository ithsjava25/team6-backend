CREATE TABLE IF NOT EXISTS incident (
                                        id BIGSERIAL PRIMARY KEY,
                                        subject VARCHAR(255) NOT NULL,
    description TEXT,
    incident_category VARCHAR(50),
    incident_status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    created_by_id VARCHAR(255) REFERENCES app_user(id),
    modified_by_id VARCHAR(255) REFERENCES app_user(id),
    assigned_to_id VARCHAR(255) REFERENCES app_user(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_incident_status ON incident(incident_status);
CREATE INDEX IF NOT EXISTS idx_incident_created_by ON incident(created_by_id);
CREATE INDEX IF NOT EXISTS idx_incident_assigned_to ON incident(assigned_to_id);
CREATE INDEX IF NOT EXISTS idx_incident_category ON incident(incident_category);

COMMENT ON TABLE incident IS 'Incidents reported by residents';
COMMENT ON COLUMN incident.id IS 'Unique incident ID (auto-generated)';
COMMENT ON COLUMN incident.subject IS 'Incident title/subject';
COMMENT ON COLUMN incident.description IS 'Detailed description of the issue';
COMMENT ON COLUMN incident.incident_category IS 'Category: LAUNDRY_ROOM, NOISE_DISTURBANCE, DAMAGE, OTHER';
COMMENT ON COLUMN incident.incident_status IS 'Status: OPEN, IN_PROGRESS, RESOLVED, CLOSED';
COMMENT ON COLUMN incident.created_by_id IS 'User who created the incident (FK to app_user)';
COMMENT ON COLUMN incident.modified_by_id IS 'User who last modified the incident';
COMMENT ON COLUMN incident.assigned_to_id IS 'Handler assigned to this incident';
COMMENT ON COLUMN incident.created_at IS 'When the incident was created';
COMMENT ON COLUMN incident.updated_at IS 'When the incident was last updated';