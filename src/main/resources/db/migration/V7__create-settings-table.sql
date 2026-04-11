CREATE TABLE settings (
    id VARCHAR(255) PRIMARY KEY,
    recipient_id VARCHAR(255) NOT NULL,
    features JSONB NOT NULL DEFAULT '[]'
);

CREATE INDEX idx_settings_recipient_id ON settings(recipient_id);
