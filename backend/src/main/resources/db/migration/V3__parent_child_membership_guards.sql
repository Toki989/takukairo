-- Enforce the same existing membership invariants for direct DB mutations.
CREATE FUNCTION participation_state_pc_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF NEW.pc_id IS NULL AND EXISTS(SELECT 1 FROM end_pc_state WHERE participation_id=NEW.id) THEN
  RAISE EXCEPTION 'PC required for existing end state' USING ERRCODE='23514';
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER participation_state_pc BEFORE UPDATE OF pc_id ON participation FOR EACH ROW EXECUTE FUNCTION participation_state_pc_guard();

CREATE FUNCTION import_trace_session_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF NOT EXISTS(SELECT 1 FROM import_candidate c JOIN import_source s ON s.import_session_id=c.import_session_id WHERE c.id=NEW.import_candidate_id AND s.id=NEW.import_source_id) THEN
  RAISE EXCEPTION 'Import trace session membership mismatch' USING ERRCODE='23503';
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER import_trace_session BEFORE INSERT OR UPDATE ON import_candidate_source_trace FOR EACH ROW EXECUTE FUNCTION import_trace_session_guard();
