-- Formal 9 Entity + named Support structures; R5.1.12. No mock-derived formal fields.
CREATE TABLE app_user (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 auth_provider VARCHAR(32) NOT NULL,
 provider_subject VARCHAR(255) NOT NULL,
 self_person_id BIGINT,
 external_image_consent_at TIMESTAMPTZ(3),
 created_at TIMESTAMPTZ(3) NOT NULL,
 updated_at TIMESTAMPTZ(3) NOT NULL,
 version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE scenario (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 user_id BIGINT NOT NULL,
 name VARCHAR(255) NOT NULL,
 author_name VARCHAR(255),
 source_url VARCHAR(2048),
 game_system VARCHAR(255),
 external_image_url VARCHAR(2048),
 image_master_key VARCHAR(512),
 image_derivative_key VARCHAR(512),
 created_at TIMESTAMPTZ(3) NOT NULL,
 updated_at TIMESTAMPTZ(3) NOT NULL,
 version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE person (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 user_id BIGINT NOT NULL,
 display_name VARCHAR(255) NOT NULL,
 created_at TIMESTAMPTZ(3) NOT NULL,
 updated_at TIMESTAMPTZ(3) NOT NULL,
 version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE pc (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 user_id BIGINT NOT NULL,
 person_id BIGINT NOT NULL,
 name VARCHAR(255) NOT NULL,
 character_sheet_url VARCHAR(2048),
 image_master_key VARCHAR(512),
 image_derivative_key VARCHAR(512),
 image_position_x NUMERIC(9,4) NOT NULL DEFAULT 0,
 image_position_y NUMERIC(9,4) NOT NULL DEFAULT 0,
 image_zoom NUMERIC(9,4) NOT NULL DEFAULT 1,
 created_at TIMESTAMPTZ(3) NOT NULL,
 updated_at TIMESTAMPTZ(3) NOT NULL,
 version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE trpg_table (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 user_id BIGINT NOT NULL,
 scenario_id BIGINT NOT NULL,
 table_name VARCHAR(255),
 recording_url VARCHAR(2048),
 created_at TIMESTAMPTZ(3) NOT NULL,
 updated_at TIMESTAMPTZ(3) NOT NULL,
 version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE table_date (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 table_id BIGINT NOT NULL,
 played_on DATE NOT NULL,
 created_at TIMESTAMPTZ(3) NOT NULL,
 updated_at TIMESTAMPTZ(3) NOT NULL,
 version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE participation (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 table_id BIGINT NOT NULL,
 person_id BIGINT NOT NULL,
 pc_id BIGINT,
 role VARCHAR(16) NOT NULL,
 display_order INTEGER NOT NULL,
 ho TEXT,
 display_quote TEXT,
 spotlight_type VARCHAR(16),
 created_at TIMESTAMPTZ(3) NOT NULL,
 updated_at TIMESTAMPTZ(3) NOT NULL,
 version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE end_pc_state (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 participation_id BIGINT NOT NULL,
 profile_key VARCHAR(128),
 status_values JSONB NOT NULL DEFAULT '{}'::jsonb,
 growth TEXT,
 outcome VARCHAR(16),
 aftereffects TEXT,
 created_at TIMESTAMPTZ(3) NOT NULL,
 updated_at TIMESTAMPTZ(3) NOT NULL,
 version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE scenario_favorite (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 user_id BIGINT NOT NULL,
 scenario_id BIGINT NOT NULL,
 created_at TIMESTAMPTZ(3) NOT NULL
);
CREATE TABLE pc_system_setting (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 pc_id BIGINT NOT NULL,
 game_system_name VARCHAR(255) NOT NULL,
 canonical_system_key VARCHAR(128),
 profile_key VARCHAR(128),
 profile_values JSONB NOT NULL DEFAULT '{}'::jsonb,
 character_sheet_url VARCHAR(2048),
 display_order INTEGER NOT NULL,
 created_at TIMESTAMPTZ(3) NOT NULL,
 updated_at TIMESTAMPTZ(3) NOT NULL
);
CREATE TABLE import_session (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 user_id BIGINT NOT NULL,
 status VARCHAR(32) NOT NULL,
 current_step VARCHAR(32) NOT NULL,
 saved_at TIMESTAMPTZ(3) NOT NULL,
 expires_at TIMESTAMPTZ(3) NOT NULL,
 completed_at TIMESTAMPTZ(3),
 delete_after_at TIMESTAMPTZ(3),
 created_at TIMESTAMPTZ(3) NOT NULL,
 updated_at TIMESTAMPTZ(3) NOT NULL,
 version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE import_source (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 import_session_id BIGINT NOT NULL,
 source_type VARCHAR(32) NOT NULL,
 original_file_name VARCHAR(255),
 encoding VARCHAR(255),
 temporary_storage_key VARCHAR(512),
 raw_text TEXT,
 source_order INTEGER NOT NULL,
 raw_byte_length BIGINT NOT NULL,
 created_at TIMESTAMPTZ(3) NOT NULL,
 updated_at TIMESTAMPTZ(3) NOT NULL,
 version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE import_candidate (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 import_session_id BIGINT NOT NULL,
 candidate_data JSONB NOT NULL DEFAULT '{}'::jsonb,
 status VARCHAR(32) NOT NULL,
 confirmation_status VARCHAR(32) NOT NULL,
 registration_target BOOLEAN NOT NULL,
 registration_status VARCHAR(32) NOT NULL,
 registration_result JSONB,
 resolution_refs JSONB NOT NULL DEFAULT '{}'::jsonb,
 created_at TIMESTAMPTZ(3) NOT NULL,
 updated_at TIMESTAMPTZ(3) NOT NULL,
 version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE import_resolution (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 import_session_id BIGINT NOT NULL,
 entity_type VARCHAR(32) NOT NULL,
 decision VARCHAR(32) NOT NULL,
 existing_entity_id BIGINT,
 draft_data JSONB,
 created_entity_id BIGINT,
 created_at TIMESTAMPTZ(3) NOT NULL,
 updated_at TIMESTAMPTZ(3) NOT NULL,
 version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE import_candidate_source_trace (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 import_candidate_id BIGINT NOT NULL,
 import_source_id BIGINT NOT NULL,
 source_location JSONB NOT NULL DEFAULT '{}'::jsonb,
 created_at TIMESTAMPTZ(3) NOT NULL,
 updated_at TIMESTAMPTZ(3) NOT NULL
);
CREATE TABLE storage_delete_task (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 object_key VARCHAR(512) NOT NULL,
 attempt_count INTEGER NOT NULL,
 next_attempt_at TIMESTAMPTZ(3) NOT NULL,
 last_error_code VARCHAR(255),
 status VARCHAR(32) NOT NULL,
 created_at TIMESTAMPTZ(3) NOT NULL
);
CREATE TABLE import_preview (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 import_session_id BIGINT NOT NULL,
 revision VARCHAR(255) NOT NULL,
 operation_kind VARCHAR(255) NOT NULL,
 session_version BIGINT NOT NULL,
 payload JSONB NOT NULL DEFAULT '{}'::jsonb,
 response JSONB NOT NULL DEFAULT '{}'::jsonb,
 created_at TIMESTAMPTZ(3) NOT NULL
);

ALTER TABLE app_user ADD UNIQUE(auth_provider,provider_subject), ADD UNIQUE(self_person_id), ADD CHECK(auth_provider='GOOGLE');
ALTER TABLE person ADD FOREIGN KEY(user_id) REFERENCES app_user(id), ADD UNIQUE(user_id,id);
ALTER TABLE scenario ADD FOREIGN KEY(user_id) REFERENCES app_user(id), ADD UNIQUE(user_id,id);
ALTER TABLE app_user ADD FOREIGN KEY(id,self_person_id) REFERENCES person(user_id,id);
ALTER TABLE pc ADD FOREIGN KEY(user_id) REFERENCES app_user(id), ADD FOREIGN KEY(user_id,person_id) REFERENCES person(user_id,id), ADD CHECK(image_zoom>0);
ALTER TABLE trpg_table ADD FOREIGN KEY(user_id) REFERENCES app_user(id), ADD FOREIGN KEY(user_id,scenario_id) REFERENCES scenario(user_id,id);
ALTER TABLE table_date ADD FOREIGN KEY(table_id) REFERENCES trpg_table(id) ON DELETE CASCADE;
ALTER TABLE participation ADD FOREIGN KEY(table_id) REFERENCES trpg_table(id) ON DELETE CASCADE, ADD FOREIGN KEY(person_id) REFERENCES person(id), ADD FOREIGN KEY(pc_id) REFERENCES pc(id), ADD CHECK(role IN ('KP','PL')), ADD CHECK(display_order>=1), ADD CHECK(spotlight_type IS NULL OR spotlight_type IN ('HO','QUOTE')), ADD CONSTRAINT participation_order UNIQUE(table_id,role,display_order) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE end_pc_state ADD FOREIGN KEY(participation_id) REFERENCES participation(id) ON DELETE CASCADE, ADD UNIQUE(participation_id), ADD CHECK(jsonb_typeof(status_values)='object'), ADD CHECK(outcome IS NULL OR outcome IN ('SURVIVED','LOST')), ADD CHECK(length(growth)<=500), ADD CHECK(length(aftereffects)<=500);
ALTER TABLE scenario_favorite ADD FOREIGN KEY(user_id) REFERENCES app_user(id) ON DELETE CASCADE, ADD FOREIGN KEY(user_id,scenario_id) REFERENCES scenario(user_id,id) ON DELETE CASCADE, ADD UNIQUE(user_id,scenario_id);
ALTER TABLE pc_system_setting ADD FOREIGN KEY(pc_id) REFERENCES pc(id) ON DELETE CASCADE, ADD CHECK(jsonb_typeof(profile_values)='object');
CREATE UNIQUE INDEX setting_canonical ON pc_system_setting(pc_id,canonical_system_key) WHERE canonical_system_key IS NOT NULL;
ALTER TABLE import_session ADD FOREIGN KEY(user_id) REFERENCES app_user(id), ADD CHECK(status IN ('ACTIVE','COMPLETED','DELETE_PENDING'));
CREATE UNIQUE INDEX import_active_user ON import_session(user_id) WHERE status='ACTIVE';
ALTER TABLE import_source ADD FOREIGN KEY(import_session_id) REFERENCES import_session(id) ON DELETE CASCADE, ADD CHECK(source_type IN ('FILE','PASTED_TEXT')), ADD CHECK(raw_byte_length>=0);
ALTER TABLE import_candidate ADD FOREIGN KEY(import_session_id) REFERENCES import_session(id) ON DELETE CASCADE;
ALTER TABLE import_resolution ADD FOREIGN KEY(import_session_id) REFERENCES import_session(id) ON DELETE CASCADE;
ALTER TABLE import_candidate_source_trace ADD FOREIGN KEY(import_candidate_id) REFERENCES import_candidate(id) ON DELETE CASCADE, ADD FOREIGN KEY(import_source_id) REFERENCES import_source(id) ON DELETE CASCADE;
ALTER TABLE import_preview ADD FOREIGN KEY(import_session_id) REFERENCES import_session(id) ON DELETE CASCADE, ADD UNIQUE(revision);
CREATE INDEX participation_pc ON participation(pc_id);
CREATE INDEX participation_person ON participation(person_id);
CREATE INDEX table_scenario ON trpg_table(scenario_id);
CREATE INDEX date_table ON table_date(table_id);
CREATE INDEX source_session ON import_source(import_session_id,source_order);
CREATE INDEX candidate_session ON import_candidate(import_session_id);
-- Child ownership is validated through parent, never a redundant user_id.
CREATE FUNCTION participation_owner_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF NOT EXISTS(SELECT 1 FROM trpg_table t JOIN person p ON p.user_id=t.user_id WHERE t.id=NEW.table_id AND p.id=NEW.person_id) OR (NEW.pc_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM trpg_table t JOIN pc p ON p.user_id=t.user_id WHERE t.id=NEW.table_id AND p.id=NEW.pc_id)) THEN RAISE EXCEPTION 'Participation ownership mismatch' USING ERRCODE='23503'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER participation_owner BEFORE INSERT OR UPDATE ON participation FOR EACH ROW EXECUTE FUNCTION participation_owner_guard();
CREATE FUNCTION end_state_pc_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN IF NOT EXISTS(SELECT 1 FROM participation WHERE id=NEW.participation_id AND pc_id IS NOT NULL) THEN RAISE EXCEPTION 'PC required for end state' USING ERRCODE='23514'; END IF; RETURN NEW; END $$;
CREATE TRIGGER end_state_pc BEFORE INSERT OR UPDATE ON end_pc_state FOR EACH ROW EXECUTE FUNCTION end_state_pc_guard();
