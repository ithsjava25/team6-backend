CREATE TABLE notification
(
    id          BIGSERIAL PRIMARY KEY,
    message     TEXT      NOT NULL,
    is_read     BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id     VARCHAR      NOT NULL,
    incident_id BIGINT    NOT NULL,

    CONSTRAINT fk_notification_user
        FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,

    CONSTRAINT fk_notification_incident
        FOREIGN KEY (incident_id) REFERENCES incident (id) ON DELETE CASCADE
);

CREATE INDEX idx_notification_user_id ON notification (user_id);
CREATE INDEX idx_notification_incident_id ON notification (incident_id);
CREATE INDEX idx_notification_created_at ON notification (created_at);