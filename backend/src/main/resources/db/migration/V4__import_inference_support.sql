-- Support metadata only. Existing candidate data and Formal Entity IDs are preserved.
ALTER TABLE import_candidate ADD COLUMN inference_data JSONB NOT NULL DEFAULT '{}'::jsonb;
