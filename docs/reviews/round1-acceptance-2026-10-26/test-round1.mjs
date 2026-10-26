// Runs only against serve-test.ps1's fresh, dedicated database and frontend.
import {createRequire} from 'node:module';
import {readFileSync,writeFileSync,mkdirSync,existsSync} from 'node:fs';
import {execFileSync} from 'node:child_process';
import {fileURLToPath} from 'node:url';
import path from 'node:path';
import assert from 'node:assert/strict';
import {createHash} from 'node:crypto';
const repo=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'../../..');
const runName=process.argv[2]??'run1',phase=process.argv[3]??'initial';
assert.match(runName,/^run\d+$/);
const run=path.join(repo,'.runtime/round1-acceptance-2026-10-26',runName);
const environment=JSON.parse(readFileSync(path.join(run,'environment.json'),'utf8').replace(/^\uFEFF/,''));
assert.equal(environment.database,'takukairo_round1_20261026_'+runName);
assert.equal(environment.frontendPort,5175);assert.equal(environment.backendPort,8082);assert.equal(environment.postgresPort,55432);
const require=createRequire(path.join(repo,'frontend/package.json'));
const {chromium,expect}=require('@playwright/test');
const expected=JSON.parse(readFileSync(path.join(run,'expected/expected_results.json'),'utf8'));
const evidence=path.join(run,'evidence');mkdirSync(evidence,{recursive:true});
const summaryFile=path.join(run,'results.json');
const results=existsSync(summaryFile)?JSON.parse(readFileSync(summaryFile,'utf8')):{startedAt:new Date().toISOString(),environment,scope:'Actual UI upload/analysis and explicit correction/registration; original archive unchanged',initial:[],registration:[],checks:[]};
function save(name,value){writeFileSync(path.join(evidence,name+'.json'),JSON.stringify(value,null,2));return value;}
function persist(){writeFileSync(summaryFile,JSON.stringify(results,null,2));}
function sql(query){return JSON.parse(execFileSync(path.join(repo,'.tools/postgres/pgsql/bin/psql.exe'),['-X','-h','127.0.0.1','-p','55432','-U','import_qa','-d',environment.database,'-At','-f','-'],{encoding:'utf8',input:Buffer.from(query,'utf8'),env:{...process.env,PGCLIENTENCODING:'UTF8'},windowsHide:true}));}
function counts(){return sql("select json_build_object('tables',(select count(*) from trpg_table),'scenarios',(select count(*) from scenario),'persons',(select count(*) from person),'pcs',(select count(*) from pc),'tableDates',(select count(*) from table_date),'participations',(select count(*) from participation),'endPcStates',(select count(*) from end_pc_state))");}
const browser=await chromium.launch({headless:true});
const context=await browser.newContext({baseURL:'http://localhost:5175',viewport:{width:1440,height:1000}});
await context.tracing.start({screenshots:true,snapshots:true,sources:true});
const page=await context.newPage();page.setDefaultTimeout(15000);
const clientErrors=[];page.on('pageerror',e=>clientErrors.push(String(e)));
async function get(url){const r=await page.request.get('/api'+url);assert.equal(r.status(),200,`${url}: ${await r.text()}`);return r.json();}
async function mutate(url,data,method='POST'){
 const csrf=await get('/csrf');const r=await page.request.fetch('/api'+url,{method,data,headers:{[csrf.headerName]:csrf.token}});
 assert.ok(r.ok(),`${url}: ${r.status()} ${await r.text()}`);return r.status()===204?null:r.json();
}
async function session(sid){return get('/import/session/'+sid);}
async function review(sid){return get('/import/session/'+sid+'/review?size=100');}
async function detail(sid,cid){return get('/import/session/'+sid+'/candidates/'+cid);}
async function resume(){await page.goto('/import');await page.getByRole('button',{name:'続きから再開',exact:true}).click();}
async function screenshot(name){await page.screenshot({path:path.join(run,'screenshots',name+'.png'),fullPage:true});}
async function intake(file){
 assert.equal(await get('/import/session/current'),null,'No unrelated active import may be removed');
 await page.goto('/import');await page.getByRole('button',{name:'インポートを始める',exact:true}).click();
 const analyze=page.getByRole('button',{name:'解析して確認へ',exact:true});await expect(analyze).toBeDisabled();
 await page.getByLabel('記録ファイルを選ぶ').setInputFiles(path.join(run,'input',file));await expect(analyze).toBeEnabled();await analyze.click();
 await expect(page.getByRole('heading',{name:'候補の確認',exact:true})).toBeVisible();
 const s=await get('/import/session/current');assert.equal(s.sources.length,1);assert.equal(s.sources[0].originalFileName,file);
 const rev=await review(s.id);await expect(page.locator('.review-row')).toHaveCount(rev.items.length);
 return {s,review:rev,details:await Promise.all(rev.items.map(r=>detail(s.id,r.candidateId)))};
}
function shape(d){return {scenario:d.scenarioName||null,table:d.tableName||null,system:d.gameSystemName??null,dates:d.tableDates.map(v=>v.playedOn).sort(),recording:d.recordingUrl??null,people:d.participations.filter(p=>p.saveDecision!=='OMIT').map(p=>({person:p.personName,role:p.role,pc:p.pcName??null,ho:p.handout??null})).sort((a,b)=>a.person.localeCompare(b.person,'ja'))};}
function expectedShape(t,corrected=false){return {scenario:corrected?(t.scenario??t.scenario_after_user_correction_example):t.scenario,table:t.table_name,system:t.game_system,dates:[...t.dates].sort(),recording:t.recording_url,people:t.participations.map(p=>({person:p.person,role:p.role,pc:p.pc,ho:p.ho})).sort((a,b)=>a.person.localeCompare(b.person,'ja'))};}
async function mergeInUi(sid,ids,label){
 const rows=(await review(sid)).items;
 for(const id of ids){const index=rows.findIndex(r=>r.candidateId===id);assert.ok(index>=0);await page.locator('.review-row').nth(index).locator('input[type=checkbox]').check();}
 await page.getByRole('button',{name:'統合',exact:true}).click();
 const dialog=page.getByRole('dialog',{name:'候補を統合',exact:true});
 await expect(dialog.getByLabel('シナリオ名',{exact:true})).toBeVisible();
 await expect(dialog.getByLabel('シナリオ名',{exact:true})).not.toHaveValue('');
 const pending=page.waitForResponse(r=>r.url().endsWith('/candidates/merge-preview')&&r.request().method()==='POST');
 await dialog.getByRole('button',{name:'変更後を確認',exact:true}).click();
 const response=await pending;save('merge-preview-'+label,{status:response.status(),body:await response.json()});
 assert.ok(response.ok(),'UI merge preview rejected: '+await response.text());
 await expect(dialog.getByRole('button',{name:'適用する',exact:true})).toBeEnabled();await screenshot('merge-preview-'+label);
 await dialog.getByRole('button',{name:'適用する',exact:true}).click();await expect(dialog).toBeHidden();
 const after=(await review(sid)).items;await expect(page.locator('.review-row')).toHaveCount(after.length);
 results.checks.push({check:'explicitUiMerge',label,sourceCandidateIds:ids,resultCount:after.length});persist();
}
async function correctAndConfirmInUi(sid,cid,t){
 const rev=await review(sid),index=rev.items.findIndex(r=>r.candidateId===cid);
 await page.locator('.review-row>button').nth(index).click();await expect(page.getByRole('heading',{name:'候補の詳細',exact:true})).toBeVisible();
 const original=(await detail(sid,cid)).candidate.candidateData,actions=[];
 const scenario=t.scenario??t.scenario_after_user_correction_example;
 if(original.scenarioName!==scenario){await page.getByLabel('シナリオ名',{exact:true}).fill(scenario);actions.push({field:'scenarioName',from:original.scenarioName,to:scenario,reason:t.fixture_id==='T05'?'Explicit test operator supplement, absent from input':'Correct mislabeled HTML value from preserved source'});}
 if(original.tableName!==t.table_name){await page.getByLabel('卓名',{exact:true}).fill(t.table_name);actions.push({field:'tableName',from:original.tableName,to:t.table_name});}
 if(JSON.stringify(original.tableDates.map(d=>d.playedOn))!==JSON.stringify(t.dates)){
  const deletes=page.getByRole('button',{name:'削除',exact:true});while(await deletes.count())await deletes.first().click();
  for(let i=0;i<t.dates.length;i++){await page.getByRole('button',{name:'＋ 日付',exact:true}).click();await page.getByLabel('開催日 '+(i+1),{exact:true}).fill(t.dates[i]);}
  actions.push({field:'tableDates',from:original.tableDates,to:t.dates,reason:'Explicit date correction from input, no parser change'});
 }
 if(t.fixture_id==='T04'){
  assert.deepEqual(original.participations.map(p=>p.personName),['参加者']);
  await page.getByLabel('参加記録の扱い',{exact:true}).selectOption('OMIT');actions.push({field:'participations',action:'OMIT',reason:'参加者 is an HTML label, not a person; raw record retained'});
 }
 const extras=page.locator('.unassigned label select');
 await expect(extras).toHaveCount(original.unassignedValues.length);
 for(let i=0;i<await extras.count();i++)await extras.nth(i).selectOption('IGNORE');
 if(original.unassignedValues.length)actions.push({field:'unassignedValues',action:'IGNORE',values:original.unassignedValues.map(u=>u.rawValue),reason:'Dates/names transferred explicitly above; headings, unknown-date text and notes remain in source'});
 await page.getByRole('button',{name:'変更を保存',exact:true}).click();await expect(page.getByText('保存済み',{exact:true})).toBeVisible();
 const corrected=(await detail(sid,cid)).candidate;save('corrected-'+t.fixture_id,{candidate:corrected,operatorActions:actions});
 assert.ok(corrected.candidateData.unassignedValues.every(u=>u.assignment.type==='IGNORE'),'Explicit UI assignment choices must be persisted');
 assert.deepEqual(shape(corrected.effectiveCandidateData??corrected.candidateData),expectedShape(t,true));
 async function bind(group,label){await group.getByLabel(label+'の判断',{exact:true}).selectOption('CREATE_NEW');await group.getByRole('button',{name:'この判断を保存',exact:true}).click();await expect(group.getByText('新規登録する判断を保存済み',{exact:true})).toBeVisible();}
 await bind(page.getByRole('group',{name:'シナリオの照合',exact:true}),'シナリオの照合');
 for(let i=0;i<corrected.candidateData.participations.length;i++){
  const p=corrected.candidateData.participations[i];if(p.saveDecision==='OMIT')continue;
  const part=page.getByRole('group',{name:'参加者 '+(i+1),exact:true});
  await bind(part.getByRole('group',{name:'人物の照合',exact:true}),'人物の照合');
  if(p.pcName)await bind(part.getByRole('group',{name:'PCの照合',exact:true}),'PCの照合');
 }
 await page.getByLabel('内容を確認済み',{exact:true}).check();await page.getByRole('button',{name:'変更を保存',exact:true}).click();
 await expect.poll(async()=>(await detail(sid,cid)).candidate.confirmationStatus).toBe('CHECKED');
 const checked=(await detail(sid,cid)).candidate;assert.equal(checked.status,'REGISTERABLE');save('confirmed-'+t.fixture_id,await detail(sid,cid));await screenshot('confirmed-'+t.fixture_id);
 results.checks.push({check:'uiCorrectionResolutionAndConfirmation',fixture:t.fixture_id,actions,candidateId:cid});persist();
 await page.getByRole('button',{name:'確認一覧へ戻る',exact:true}).click();await expect(page.getByRole('heading',{name:'候補の確認',exact:true})).toBeVisible();
}
function formalShape(d){return {scenario:d.scenario.name,table:d.tableName,system:d.scenario.gameSystem??null,dates:d.tableDates.map(v=>v.playedOn).sort(),recording:d.recordingUrl??null,people:d.participations.map(p=>({person:p.person.displayName,role:p.role,pc:p.pc?.name??null,ho:p.ho??null})).sort((a,b)=>a.person.localeCompare(b.person,'ja'))};}
try{
 await page.goto('/dev-login');await page.getByRole('button',{name:'固定テストユーザーでログイン',exact:true}).click();
 await expect(page.getByRole('heading',{name:/あなたの回廊|TRPGで使う名前/})).toBeVisible();
 if(await page.getByRole('heading',{name:'TRPGで使う名前',exact:true}).isVisible()){
  await page.getByLabel('表示名',{exact:true}).fill('第1回検証専用');await page.getByRole('button',{name:'設定して始める',exact:true}).click();
 }
 await expect(page.getByRole('heading',{name:'あなたの回廊',exact:true})).toBeVisible();
 if(phase==='initial'){
  results.baseline=counts();assert.deepEqual(results.baseline,{tables:0,scenarios:0,persons:1,pcs:0,tableDates:0,participations:0,endPcStates:0});
  for(const file of [...new Set(expected.tables.map(t=>t.file))]){
   const r=await intake(file),sid=r.s.id,tables=expected.tables.filter(t=>t.file===file);
   save('initial-'+file,r);await screenshot('initial-'+file);
   const actual=r.details.map(d=>shape(d.candidate.candidateData)),target=tables.map(t=>expectedShape(t));
   const match=JSON.stringify(actual)===JSON.stringify(target);
   const source=r.s.sources[0];const stored=sql(`select json_build_object('rawText',raw_text,'storageKey',temporary_storage_key) from import_source where id=${source.id}`);
   const original=readFileSync(path.join(run,'input',file));
   const storedBytes=stored.storageKey?readFileSync(path.join(run,'storage',stored.storageKey)):Buffer.from(stored.rawText,'utf8');
   assert.deepEqual(storedBytes,original,'Uploaded input bytes must be preserved');
   await page.getByRole('button',{name:'正式登録へ',exact:true}).click();await expect(page.getByRole('heading',{name:'正式登録の確認',exact:true})).toBeVisible();
   await expect(page.getByRole('button',{name:/対象を正式登録する/})).toBeDisabled();
   await screenshot('unconfirmed-registration-'+file);
   results.initial.push({file,sessionId:sid,expectedCandidates:tables.length,actualCandidates:r.review.items.length,unparsedBlocks:r.review.unparsedBlocks.length,automaticStructureMatches:match,actual,expected:target,unconfirmedRegistrationDisabled:true,sourcePreserved:true,inputSha256:createHash('sha256').update(original).digest('hex')});persist();
   // These are this run's own unregistered inspection sessions, never normal app sessions.
   await mutate('/import/session/'+sid+'?expectedSessionVersion='+(await session(sid)).version,undefined,'DELETE');
   console.log(JSON.stringify(results.initial.at(-1)));
  }
  assert.deepEqual(counts(),results.baseline);results.initialPhase='COMPLETED';
 }else if(phase==='registration'||phase==='registration-resume'){
  assert.equal(results.initialPhase,'COMPLETED');if(phase==='registration')assert.deepEqual(counts(),results.baseline);delete results.error;
  for(const file of [...new Set(expected.tables.map(t=>t.file))]){
   const tables=expected.tables.filter(t=>t.file===file);if(tables.every(t=>results.registration.some(r=>r.fixture===t.fixture_id)))continue;
   console.log('Registration input: '+file);const active=await get('/import/session/current');let sid;
   if(active){assert.equal(phase,'registration-resume');assert.equal(active.sources[0].originalFileName,file);sid=active.id;await resume();if(active.currentStep==='REGISTER')await page.getByRole('button',{name:'確認へ戻る',exact:true}).click();}
   else{const r=await intake(file);sid=r.s.id;save('before-correction-'+file,r);}
   if(file.endsWith('.csv'))for(const t of tables){const ids=(await review(sid)).items.filter(v=>v.rawSummary.tableName===t.table_name).map(v=>v.candidateId);assert.ok(ids.length===3||ids.length===1);if(ids.length===3)await mergeInUi(sid,ids,t.fixture_id);}
   if(file.endsWith('.md')){if((await review(sid)).items.length>1)await mergeInUi(sid,(await review(sid)).items.map(v=>v.candidateId),'T03');const note=page.locator('.unparsed-record');await expect(note).toHaveCount(1);await note.getByLabel('この活動記録を今回は保存しない',{exact:true}).check();await expect.poll(async()=>(await review(sid)).unparsedBlocks[0].state).toBe('EXCLUDED');results.checks.push({check:'uiExcludeAnnotation',file,reason:'The remaining block is a note that the date and PC are missing, not another table'});}
   const rows=(await review(sid)).items;assert.equal(rows.length,tables.length);
   for(let i=0;i<tables.length;i++){
    const t=tables[i],row=file.endsWith('.csv')?rows.find(v=>v.rawSummary.tableName===t.table_name):rows[0];
    await correctAndConfirmInUi(sid,row.candidateId,t);
   }
   await page.getByRole('button',{name:'正式登録へ',exact:true}).click();await expect(page.getByRole('heading',{name:'正式登録の確認',exact:true})).toBeVisible();
   const register=page.getByRole('button',{name:/対象を正式登録する/});await expect(register).toBeEnabled();
   const pending=page.waitForResponse(v=>v.url().endsWith('/register')&&v.request().method()==='POST');await register.click();const response=await pending;const body=await response.json();save('registration-'+file,{status:response.status(),body});
   assert.equal(response.status(),200);assert.equal(body.results.length,tables.length);assert.ok(body.results.every(r=>r.outcome==='REGISTERED'),JSON.stringify(body));await expect(page.getByRole('link',{name:'卓を開く',exact:true})).toHaveCount(tables.length);await screenshot('registered-'+file);
   for(const rr of body.results){
    const formal=await get('/tables/'+rr.registeredTableId),t=tables.find(t=>t.table_name===formal.tableName);assert.ok(t);save('formal-'+t.fixture_id,formal);assert.deepEqual(formalShape(formal),expectedShape(t,true));
    assert.ok(formal.participations.every(p=>p.endPcState===null));
    for(const p of formal.participations)if(p.pc)assert.equal(p.pc.person.id,p.person.id,'PC ownership must match its participation person');
    await page.goto('/tables/'+formal.id);await expect(page.getByRole('main',{name:'卓詳細',exact:true})).toBeVisible();await page.getByRole('button',{name:'卓の情報を見る',exact:true}).click();const memory=page.getByRole('dialog',{name:'卓の情報',exact:true});await expect(memory.getByRole('heading',{name:formal.scenario.name,exact:true})).toBeVisible();await expect(memory.getByText(t.table_name,{exact:true})).toBeVisible();await screenshot('formal-'+t.fixture_id);
    results.registration.push({fixture:t.fixture_id,tableId:formal.id,outcome:'REGISTERED',formalMatches:true,actual:formalShape(formal)});persist();console.log('Registered and verified: '+t.fixture_id);
   }
   await resume();await page.getByRole('button',{name:'インポートを完了する',exact:true}).click();await expect.poll(()=>get('/import/session/current')).toBe(null);save('completed-session-'+sid,await session(sid));
  }
  results.finalCounts=counts();assert.deepEqual(results.finalCounts,{tables:5,scenarios:5,persons:11,pcs:5,tableDates:5,participations:10,endPcStates:0});
  results.newEntityCounts={...results.finalCounts,persons:results.finalCounts.persons-results.baseline.persons};
  const forbidden=sql("select json_build_object('people',(select count(*) from person where display_name in ('参加者','0名（名簿の記録なし）','未記録')),'scenarios',(select count(*) from scenario where name='シナリオ名'),'tables',(select count(*) from trpg_table where table_name='卓名'))");assert.deepEqual(forbidden,{people:0,scenarios:0,tables:0});results.forbiddenLabelEntities=forbidden;
  const html=results.registration.find(r=>r.fixture==='T04'),md=results.registration.find(r=>r.fixture==='T03');assert.deepEqual(html.actual.people,[]);assert.deepEqual(md.actual.dates,[]);assert.ok(md.actual.people.every(p=>p.pc===null));
  const sources=sql("select json_agg(json_build_object('file',original_file_name,'rawText',raw_text,'storageKey',temporary_storage_key)) from import_source");assert.equal(sources.length,4);
  results.finalSourceIntegrity=sources.map(s=>{const original=readFileSync(path.join(run,'input',s.file)),stored=s.storageKey?readFileSync(path.join(run,'storage',s.storageKey)):Buffer.from(s.rawText,'utf8');assert.deepEqual(stored,original);return {file:s.file,sha256:createHash('sha256').update(stored).digest('hex'),preserved:true};});
  results.registrationPhase='COMPLETED';await page.goto('/');await expect(page.getByRole('heading',{name:'あなたの回廊',exact:true})).toBeVisible();await screenshot('final-home');
 }else throw new Error('Unknown phase '+phase);
 results.clientErrors=clientErrors;assert.deepEqual(clientErrors,[]);persist();
}catch(e){results.error={phase,message:String(e),stack:e.stack};persist();await screenshot('failure-'+phase).catch(()=>{});throw e;}
finally{await context.tracing.stop({path:path.join(run,'trace-'+phase+'.zip')});await browser.close();}
