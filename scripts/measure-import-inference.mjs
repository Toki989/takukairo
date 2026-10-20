// Run only from qa-import-inference.ps1, against its dedicated browser database.
import {createRequire} from 'node:module';
import {writeFileSync} from 'node:fs';
import {execFileSync} from 'node:child_process';
import {fileURLToPath} from 'node:url';
import path from 'node:path';
import assert from 'node:assert/strict';
import os from 'node:os';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const require=createRequire(path.join(root,'frontend/package.json'));
const {request}=require('@playwright/test');
assert.equal(process.env.TAKUKAIRO_IMPORT_SCALE_ORIGIN,'http://127.0.0.1:8082');
assert.match(process.env.TAKUKAIRO_IMPORT_QA_PID??'',/^\d+$/);
const pid=process.env.TAKUKAIRO_IMPORT_QA_PID;
const api=await request.newContext({baseURL:process.env.TAKUKAIRO_IMPORT_SCALE_ORIGIN,timeout:180000});
async function get(url){const r=await api.get('/api'+url);assert.equal(r.status(),200);return r.json();}
async function mutate(url,data,method='POST'){
 const csrf=await get('/csrf');const r=await api.fetch('/api'+url,{method,data,headers:{[csrf.headerName]:csrf.token}});assert.ok(r.ok(),`${url}: ${r.status()} ${(await r.text()).slice(0,500)}`);return r;
}
await mutate('/dev/session');
const output={dateLocal:'2026-10-15 Asia/Tokyo',scope:'Dedicated real HTTP/PostgreSQL/Storage; synthetic scalability fixtures, not T01–T24',environment:{cpu:os.cpus()[0].model,logicalCpus:os.cpus().length,systemMemoryBytes:os.totalmem(),node:process.version},cases:[]};
function memory(){
 const heap=execFileSync(path.join(root,'.tools/java/jdk-25.0.4.1+1/bin/jcmd.exe'),[pid,'GC.heap_info'],{encoding:'utf8',windowsHide:true});
 const os=JSON.parse(execFileSync('powershell.exe',['-NoProfile','-Command',`$p=Get-Process -Id ${pid}; @{workingSetBytes=$p.WorkingSet64;peakWorkingSetBytes=$p.PeakWorkingSet64;cpuSeconds=$p.CPU}|ConvertTo-Json -Compress`],{encoding:'utf8',windowsHide:true}));
 execFileSync(path.join(root,'.tools/java/jdk-25.0.4.1+1/bin/jcmd.exe'),[pid,'GC.run'],{windowsHide:true});
 const retainedHeap=execFileSync(path.join(root,'.tools/java/jdk-25.0.4.1+1/bin/jcmd.exe'),[pid,'GC.heap_info'],{encoding:'utf8',windowsHide:true});
 return {...os,heapInfo:heap,heapAfterDiagnosticFullGc:retainedHeap,note:'OS peak is cumulative for this Backend process. Diagnostic full GC runs after all timings, between fixtures; heap snapshots are not allocation peaks.'};
}
async function run(name,files,expected){
 const old=await get('/import/session/current');if(old)await mutate('/import/session/'+old.id+'?expectedSessionVersion='+old.version,undefined,'DELETE');
 const session=await(await mutate('/import/session')).json();const csrf=await get('/csrf');
 const form=new FormData();form.append('expectedSessionVersion',String(session.version));for(const f of files)form.append('files',new Blob([f.body],{type:'text/plain'}),f.name);
 const uploaded=await api.post('/api/import/session/'+session.id+'/sources/files',{headers:{[csrf.headerName]:csrf.token},multipart:form});
 assert.ok(uploaded.ok(),await uploaded.text());const upload=await uploaded.json();
 const start=performance.now();const response=await mutate('/import/session/'+session.id+'/analyze',{expectedSessionVersion:upload.sessionVersion});const analyzeMs=performance.now()-start;const analyzeBytes=(await response.body()).length;
 const reviewStart=performance.now();const reviewResponse=await api.get('/api/import/session/'+session.id+'/review?size=100');assert.equal(reviewResponse.status(),200);const reviewBuffer=await reviewResponse.body();const review=JSON.parse(reviewBuffer);assert.equal(review.summary.total,expected);const reviewMs=performance.now()-reviewStart;
 const detailStart=performance.now();const detailResponse=await api.get('/api/import/session/'+session.id+'/candidates/'+review.items[0].candidateId);assert.equal(detailResponse.status(),200);const detailBuffer=await detailResponse.body();const detail=JSON.parse(detailBuffer);const detailMs=performance.now()-detailStart;
 const pairs=detail.candidate.inference.boundaryAssessments;assert.equal(pairs.length,expected-1);assert.ok(pairs.every(p=>!p.automaticMerge));
 assert.ok(Number.isSafeInteger(session.id));
 const sql=`select json_build_object('candidateCount',count(*),'metadataBytes',coalesce(sum(octet_length(inference_data::text)),0),'persistedPairCount',coalesce(sum(jsonb_array_length(coalesce(inference_data->'boundaryAssessments','[]'::jsonb))),0)) from import_candidate where import_session_id=${session.id}`;
 const database=JSON.parse(execFileSync(path.join(root,'.tools/postgres/pgsql/bin/psql.exe'),['-h','127.0.0.1','-p','55432','-U','import_qa','-d','takukairo_import_browser_qa','-Atc',sql],{encoding:'utf8',windowsHide:true}));assert.equal(database.candidateCount,expected);assert.equal(database.persistedPairCount,0);
 const record={name,inputBytes:files.reduce((n,f)=>n+f.body.length,0),files:files.length,candidates:expected,analyzeMs,analyzeBytes,reviewMs,reviewBytes:reviewBuffer.length,detailMs,detailBytes:detailBuffer.length,detailAssessments:pairs.length,database,memory:memory()};output.cases.push(record);writeFileSync(path.join(root,'.runtime/import-review/scale-results.json'),JSON.stringify(output,null,2));console.log(JSON.stringify({...record,memory:{...record.memory,heapInfo:undefined}}));
}
try{
 for(const n of [100,300,600,1500]){const csv='シナリオ名,卓名\n'+Array.from({length:n},(_,i)=>'A,T'+i).join('\n');await run('dense-'+n,[{name:'dense.csv',body:Buffer.from(csv)}],n);}
 for(const mib of [20,50]){
  const sizes=mib===20?[20]:[20,20,10];const files=sizes.map((size,i)=>{const prefix=Buffer.from('シナリオ: A\n卓名: T'+i+'\n');return {name:'size-'+i+'.txt',body:Buffer.concat([prefix,Buffer.alloc(size*1024*1024-prefix.length,32)])};});await run('size-'+mib+'MiB',files,files.length);
 }
}finally{await api.dispose();}
