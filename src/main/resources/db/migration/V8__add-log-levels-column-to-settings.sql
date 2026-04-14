ALTER TABLE settings ADD COLUMN log_levels jsonb DEFAULT '[]'::jsonb;
