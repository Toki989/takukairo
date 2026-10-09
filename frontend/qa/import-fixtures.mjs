import {request} from '@playwright/test';
const state=process.argv[2]??'review';if(!['none','review','almost-complete'].includes(state))throw new Error('State: none | review | almost-complete');
const ctx=await request.newContext({baseURL:'http://localhost:5173'});
async function read(path){const r=await ctx.get('/api'+path);if(!r.ok())throw new Error(`GET ${path}: ${r.status()}`);return r.json()}
async function change(path,method,data){const csrf=await read('/csrf');const r=await ctx.fetch('/api'+path,{method,data,headers:{[csrf.headerName]:csrf.token}});if(!r.ok())throw new Error(`${method} ${path}: ${r.status()} ${await r.text()}`);return r.status()===204?null:r.json()}
try{
 await change('/dev/session','POST');const self=await read('/session');if(!self.user?.selfPerson)await change('/self-person','POST',{displayName:'テストPL'});
 const active=await read('/import/session/current');if(active){if(!process.argv.includes('--discard-current'))throw new Error('途中保存があります。Dataを保全して終了します。破棄する場合だけ --discard-current を指定してください。');await change('/import/session/'+active.id+'?expectedSessionVersion='+active.version,'DELETE')}
 if(state==='none'){console.log('Current Import Session: none');process.exitCode=0}else{
  const session=await change('/import/session','POST'),base='/import/session/'+session.id;
  const fresh=()=>read(base);await change(base+'/sources/text','POST',{expectedSessionVersion:(await fresh()).version,text:'シナリオ: QA 登録可能\nゲームシステム: クトゥルフ神話TRPG 7版\n\nシナリオ: QA 照合待ち\n確認が必要な値\n\n割り当て前の記録'});await change(base+'/analyze','POST',{expectedSessionVersion:(await fresh()).version});
  const review=await read(base+'/review'),first=review.items[0];await change(base+'/candidates/'+first.candidateId,'PATCH',{expectedSessionVersion:(await fresh()).version,expectedVersion:first.version,confirmationStatus:'CHECKED',resolutionChanges:[{slot:{type:'SCENARIO'},decision:'CREATE_NEW',draftData:{name:'QA 登録可能',gameSystem:'クトゥルフ神話TRPG 7版'}}]});
  if(state==='almost-complete'){
   const candidate=(await read(base+'/candidates/'+first.candidateId)).candidate;await change(base+'/register','POST',{expectedSessionVersion:(await fresh()).version,candidates:[{candidateId:candidate.id,expectedVersion:candidate.version}]});
   for(const row of review.items.slice(1)){const c=(await read(base+'/candidates/'+row.candidateId)).candidate;await change(base+'/candidates/'+c.id,'PATCH',{expectedSessionVersion:(await fresh()).version,expectedVersion:c.version,registrationTarget:false})}
  }
  await change(base,'PATCH',{expectedSessionVersion:(await fresh()).version,currentStep:state==='almost-complete'?'REGISTER':'REVIEW'});
  const final=await read(base+'/review');console.log(JSON.stringify({state,sessionId:session.id,summary:final.summary,items:final.items.map(c=>({candidateId:c.candidateId,status:c.status,registrationStatus:c.registrationStatus}))},null,2));console.log('http://localhost:5173/import → 続きから再開');
 }
}finally{await ctx.dispose()}
