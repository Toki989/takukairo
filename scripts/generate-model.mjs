import fs from 'node:fs';
const root='backend/src/main';
const tables={
 AppUser:['app_user',{authProvider:'String!',providerSubject:'String!',selfPersonId:'Long',externalImageConsentAt:'Instant'}],
 Scenario:['scenario',{userId:'Long!',name:'String!',authorName:'String',sourceUrl:'String',gameSystem:'String',externalImageUrl:'String',imageMasterKey:'String',imageDerivativeKey:'String'}],
 Person:['person',{userId:'Long!',displayName:'String!'}],
 Pc:['pc',{userId:'Long!',personId:'Long!',name:'String!',characterSheetUrl:'String',imageMasterKey:'String',imageDerivativeKey:'String',imagePositionX:'BigDecimal!',imagePositionY:'BigDecimal!',imageZoom:'BigDecimal!'}],
 TrpgTable:['trpg_table',{userId:'Long!',scenarioId:'Long!',tableName:'String',recordingUrl:'String'}],
 TableDate:['table_date',{tableId:'Long!',playedOn:'LocalDate!'}],
 Participation:['participation',{tableId:'Long!',personId:'Long!',pcId:'Long',role:'String!',displayOrder:'Integer!',ho:'Text',displayQuote:'Text',spotlightType:'String'}],
 EndPcState:['end_pc_state',{participationId:'Long!',profileKey:'String',statusValues:'Json!',growth:'Text',outcome:'String',aftereffects:'Text'}],
 ScenarioFavorite:['scenario_favorite',{userId:'Long!',scenarioId:'Long!'}],
 PcSystemSetting:['pc_system_setting',{pcId:'Long!',gameSystemName:'String!',canonicalSystemKey:'String',profileKey:'String',profileValues:'Json!',characterSheetUrl:'String',displayOrder:'Integer!'}],
 ImportSession:['import_session',{userId:'Long!',status:'String!',currentStep:'String!',savedAt:'Instant!',expiresAt:'Instant!',completedAt:'Instant',deleteAfterAt:'Instant'}],
 ImportSource:['import_source',{importSessionId:'Long!',sourceType:'String!',originalFileName:'String',encoding:'String',temporaryStorageKey:'String',rawText:'Text',sourceOrder:'Integer!',rawByteLength:'Long!'}],
 ImportCandidate:['import_candidate',{importSessionId:'Long!',candidateData:'Json!',status:'String!',confirmationStatus:'String!',registrationTarget:'Boolean!',registrationStatus:'String!',registrationResult:'Json',resolutionRefs:'Json!'}],
 ImportResolution:['import_resolution',{importSessionId:'Long!',entityType:'String!',decision:'String!',existingEntityId:'Long',draftData:'Json',createdEntityId:'Long'}],
 ImportCandidateSourceTrace:['import_candidate_source_trace',{importCandidateId:'Long!',importSourceId:'Long!',sourceLocation:'Json!'}],
 StorageDeleteTask:['storage_delete_task',{objectKey:'String!',attemptCount:'Integer!',nextAttemptAt:'Instant!',lastErrorCode:'String',status:'String!'}],
 ImportPreview:['import_preview',{importSessionId:'Long!',revision:'String!',operationKind:'String!',sessionVersion:'Long!',payload:'Json!',response:'Json!'}]
};
const snake=s=>s.replace(/[A-Z]/g,c=>'_'+c.toLowerCase());
const noVersion=new Set(['ScenarioFavorite','PcSystemSetting','ImportCandidateSourceTrace','StorageDeleteTask','ImportPreview']);
const noUpdated=new Set(['ScenarioFavorite','StorageDeleteTask','ImportPreview']);
let sql='-- Formal 9 Entity + named Support structures; R5.1.12. No mock-derived formal fields.\n';
for(const [cls,[table,raw]] of Object.entries(tables)){
 const fields={...raw,createdAt:'Instant!'};
 if(!noUpdated.has(cls))fields.updatedAt='Instant!';
 let java=`package jp.takukairo.model;\nimport jakarta.persistence.*;\nimport java.time.*;\nimport java.math.BigDecimal;\nimport org.hibernate.annotations.JdbcTypeCode;\nimport org.hibernate.type.SqlTypes;\n@Entity @Table(name="${table}")\npublic class ${cls} {\n @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;\n`;
 const columns=['id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY'];
 for(const [field,spec] of Object.entries(fields)){
  const required=spec.endsWith('!'),type=spec.replace('!',''),column=snake(field);
  let db={Long:'BIGINT',Integer:'INTEGER',Boolean:'BOOLEAN',Instant:'TIMESTAMPTZ(3)',LocalDate:'DATE',BigDecimal:'NUMERIC(9,4)',Text:'TEXT',Json:'JSONB'}[type]||'VARCHAR(255)';
  if(type==='String'){if(/Key$/.test(field)&&!['profileKey','canonicalSystemKey'].includes(field))db='VARCHAR(512)';if(/Url$/.test(field))db='VARCHAR(2048)';if(['profileKey','canonicalSystemKey'].includes(field))db='VARCHAR(128)';if(['role','outcome','spotlightType'].includes(field))db='VARCHAR(16)';if(['authProvider','status','currentStep','entityType','decision','sourceType','confirmationStatus','registrationStatus'].includes(field))db='VARCHAR(32)';}
  java+=` ${type==='Json'?'@JdbcTypeCode(SqlTypes.JSON) ':''}@Column(name="${column}"${['Text','Json'].includes(type)?`, columnDefinition="${db.toLowerCase()}"`:type==='String'?`, length=${Number(db.match(/\d+/)[0])}`:type==='BigDecimal'?', precision=9, scale=4':''}) public ${['Json','Text'].includes(type)?'String':type} ${field}${type==='Json'?' = "{}"':field==='imageZoom'?' = BigDecimal.ONE':type==='BigDecimal'?' = BigDecimal.ZERO':type==='Instant'&&required?' = Instant.now()':''};\n`;
  columns.push(`${column} ${db}${required?' NOT NULL':''}${type==='Json'&&required?" DEFAULT '{}'::jsonb":field==='imageZoom'?' DEFAULT 1':type==='BigDecimal'?' DEFAULT 0':''}`);
 }
 if(!noVersion.has(cls)){java+=' @Version public long version;\n';columns.push('version BIGINT NOT NULL DEFAULT 0');}
 java+='}\n';fs.mkdirSync(root+'/java/jp/takukairo/model',{recursive:true});fs.writeFileSync(root+'/java/jp/takukairo/model/'+cls+'.java',java);
 sql+=`CREATE TABLE ${table} (\n ${columns.join(',\n ')}\n);\n`;
}
sql+=`
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
`;
fs.mkdirSync(root+'/resources/db/migration',{recursive:true});fs.writeFileSync(root+'/resources/db/migration/V1__domain_and_support.sql',sql);
// Exact versioned config is extracted from normative JSON, not the prototype.
const spec=fs.readFileSync(fs.readdirSync('.').find(f=>f.includes('R5.1.12')),'utf8');
const blocks=[...spec.matchAll(/```json\r?\n([\s\S]*?)\r?\n```/g)].map(m=>{try{return JSON.parse(m[1]);}catch{return null;}}).filter(Boolean);
const options=blocks.find(x=>x.sourceKey==='emoklore_resonance_emotions');
const coc=blocks.find(x=>x.profileKey==='coc_6e_v1'&&x.endStateStatuses);
const emo=blocks.find(x=>x.profileKey==='emoklore_v1'&&x.pcFields);
emo.optionSources=[options];
const profiles=[coc,{...coc,profileKey:'coc_7e_v1',canonicalSystemKey:'coc_7e',displayName:'クトゥルフ神話TRPG 7版'},emo];
fs.mkdirSync(root+'/resources/config',{recursive:true});fs.writeFileSync(root+'/resources/config/profiles.json',JSON.stringify(profiles,null,2));
console.log('Generated 9 formal entities + 8 support mappings; normative profiles and migration.');
