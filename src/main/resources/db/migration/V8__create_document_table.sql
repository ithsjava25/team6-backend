CREATE TABLE document(
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255),
    content_type VARCHAR(255),
    file_key VARCHAR(255) NOT NULL,
    file_size BIGINT,

    incident_id BIGINT NOT NULL,

    CONSTRAINT fk_document_incident
                     FOREIGN KEY (incident_id)
                     REFERENCES incident(id)
                     ON DELETE CASCADE
);