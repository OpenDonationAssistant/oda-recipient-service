ALTER TABLE webhook ADD COLUMN settings jsonb DEFAULT '{}'::jsonb;
