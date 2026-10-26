import {chromium,expect} from '../../frontend/node_modules/@playwright/test/index.mjs';
import {writeFile} from 'node:fs/promises';
const origin='http://localhost:5176';
const browser=await chromium.launch({headless:true});
const context=await browser.newContext({baseURL:origin});
const page=await context.newPage();
const evidence=[];
async function mutation(path,method,data,multipart){
 const csrf=await(await page.request.get('/api/csrf')).json();
 const r=await page.request.fetch('/api'+path,{method,headers:{[csrf.headerName]:csrf.token},...(multipart?{multipart}:{data})});
 if(!r.ok())throw Error(`${method} ${path}: ${r.status()} ${await r.text()}`);
 return r.status()===204?null:r.json();
}
async function fresh(name,raw){
 const old=await(await page.request.get('/api/import/session/current')).json();
 if(old)await mutation('/import/session/'+old.id+'?expectedSessionVersion='+old.version,'DELETE');
 const session=await mutation('/import/session','POST');const base='/import/session/'+session.id;
 const added=name?await mutation(base+'/sources/files?expectedSessionVersion='+session.version,'POST',undefined,{files:{name,mimeType:'text/plain',buffer:Buffer.from(raw)}}):await mutation(base+'/sources/text','POST',{expectedSessionVersion:session.version,text:raw});
 await mutation(base+'/analyze','POST',{expectedSessionVersion:added.sessionVersion});
 const review=await(await page.request.get('/api'+base+'/review')).json();return {session,base,review};
}
try{
 await page.goto(origin+'/dev-login');await page.getByRole('button',{name:'固定テストユーザーでログイン'}).click();await expect(page.getByRole('heading',{name:'あなたの回廊'})).toBeVisible();
 for(const [id,name,raw] of [
  ['date-before-md-table','review.md','シナリオ: A\n日付: 2026-10-01\n\n|PL|PC|\n|---|---|\n|田中|青い月|'],
  ['negative-context','review.csv','シナリオ名,記録,氏名,PC\nA,プレイ済みではなかった,田中,青い月'],
  ['main-skp-subheadings','review.md','# シナリオ: A\n## 卓名: T\n### 参加者\nKP: 佐藤\n### その他\nSKP: 田中']
 ]){
  const {base,review}=await fresh(name,raw);const detail=await(await page.request.get('/api'+base+'/candidates/'+review.items[0].candidateId)).json();
  const d=detail.candidate.candidateData;
  const reproduced=id==='date-before-md-table'?d.tableDates.length===0:id==='negative-context'?d.participations[0].role==='PL':d.participations.find(p=>p.personName==='田中').role===null;
  evidence.push({id,reproduced,input:raw,review,detail});
 }
 const {base,review}=await fresh(null,'シナリオ: 分割割当検証\nPL: 田中\nPC: 元PC\n新PC');
 await page.goto(origin+'/import');await page.getByRole('button',{name:'続きから再開'}).click();await page.locator('.review-row>button').first().click();
 await page.getByRole('combobox',{name:'この値の扱い',exact:true}).selectOption('PARTICIPATION_PC');
 await page.getByLabel('割り当てる参加者',{exact:true}).selectOption(review.items[0].summary.participations[0].candidateParticipationKey);
 await expect(page.getByLabel('PC名',{exact:true})).toHaveValue('新PC');await page.getByRole('button',{name:'変更を保存',exact:true}).click();
 await expect.poll(async()=>{const d=await(await page.request.get('/api'+base+'/candidates/'+review.items[0].candidateId)).json();return d.candidate.effectiveCandidateData.participations[0].pcName}).toBe('新PC');
 await page.getByRole('button',{name:'確認一覧へ戻る'}).click();await page.getByLabel('分割割当検証を選択',{exact:true}).check();await page.getByRole('button',{name:'分割',exact:true}).click();
 await page.getByRole('button',{name:'次の分割先へ移す',exact:true}).click();await page.getByRole('button',{name:'変更後を確認',exact:true}).click();
 await expect(page.getByRole('button',{name:'適用する',exact:true})).toBeEnabled();await page.getByRole('button',{name:'適用する',exact:true}).click();await expect(page.getByRole('dialog')).toHaveCount(0);
 const after=await(await page.request.get('/api'+base+'/review')).json();const details=[];for(const r of after.items)details.push(await(await page.request.get('/api'+base+'/candidates/'+r.candidateId)).json());
 const moved=details.find(d=>d.candidate.candidateData.participations.length);const orphan=details.find(d=>!d.candidate.candidateData.participations.length);
 const reproduced=moved.candidate.effectiveCandidateData.participations[0].pcName==='元PC'&&orphan.candidate.status==='NEEDS_FIX'&&orphan.candidate.candidateData.unassignedValues[0].rawValue==='新PC';
 evidence.push({id:'split-assigned-person-moved-browser',reproduced,review:after,details});
 await page.screenshot({path:new URL('./split-after.png',import.meta.url).pathname.replace(/^\/([A-Z]:)/,'$1'),fullPage:true});
 if(evidence.some(e=>!e.reproduced))throw Error('At least one observation did not reproduce');
}catch(error){evidence.push({error:String(error)});process.exitCode=1;}
finally{await writeFile(new URL('./browser-probes.json',import.meta.url),JSON.stringify({officialAcceptance:false,scope:'Isolated real HTTP/PostgreSQL and browser; counterexamples, not acceptance cases',evidence},null,2));await browser.close();}
