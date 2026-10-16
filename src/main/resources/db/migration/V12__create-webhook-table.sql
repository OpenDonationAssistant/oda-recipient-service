CREATE TABLE webhook (
    id UUID PRIMARY KEY,
    recipient_id VARCHAR(255) NOT NULL,
    url VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_webhook_recipient_id ON webhook(recipient_id);
