import {test} from 'node:test';
import assert from 'node:assert/strict';
import {candidatePatch,resolutionMismatch,resolutionName,assignedData,editAssignedField,moveSplitParticipant} from '../src/features/import-state.ts';
const draft={scenarioName:'A',gameSystemName:null,tableName:null,tableDates:[],recordingUrl:null,participations:[],unassignedValues:[]};
const saved={candidateData:draft,confirmationStatus:'CHECKED',registrationTarget:true};
test('unchanged PATCH omits confirmation, draft and target',()=>assert.deepEqual(candidatePatch(saved,draft,true,true,[]),{}));
test('edit clears checked without automatically resending old checked',()=>{const patch=candidatePatch(saved,{...draft,scenarioName:'B'},false,true,[]);assert.equal(patch.confirmationStatus,'UNCHECKED');assert.equal(patch.candidateData.scenarioName,'B');assert.equal('registrationTarget'in patch,false)});
test('resolution mismatch shows saved draft and never mutates shared resolution',()=>{const r={entityType:'SCENARIO',decision:'CREATE_NEW',draftData:{name:'saved-name',gameSystem:null}};assert.equal(resolutionName(r),'saved-name');assert.equal(resolutionMismatch(r,'edited-name'),true);assert.equal(r.draftData.name,'saved-name');assert.equal(resolutionMismatch({...r,draftData:{name:'edited-name',gameSystem:'CoC'}},'edited-name','Other'),true)});
test('explicit confirm remains a distinct operation from autosave',()=>assert.deepEqual(candidatePatch({...saved,confirmationStatus:'UNCHECKED'},draft,true,true,[]),{confirmationStatus:'CHECKED'}));

test('same PATCH includes CHECKED only after explicit reconfirmation even when baseline was checked',()=>{const edited={...draft,tableName:'final'};assert.equal(candidatePatch(saved,edited,true,true,[],true).confirmationStatus,'CHECKED');assert.equal('confirmationStatus'in candidatePatch(saved,edited,true,true,[]),false)});
test('the acknowledged B response is the baseline when an in-flight edit returns to A',()=>{const a={...draft,tableName:'A'},b={...draft,tableName:'B'};const first=candidatePatch({...saved,candidateData:a},b,false,true,[]);assert.equal(first.candidateData.tableName,'B');const acknowledged={...saved,candidateData:b,confirmationStatus:'UNCHECKED'};assert.equal(candidatePatch(acknowledged,a,false,true,[]).candidateData.tableName,'A');assert.deepEqual(candidatePatch({...acknowledged,candidateData:a},a,false,true,[]),{})});
test('assignment projection and release preserve original draft, omissions and conflicting values',()=>{
 for(const [type,field] of [['PARTICIPATION_PERSON','personName'],['PARTICIPATION_PC','pcName'],['PARTICIPATION_HANDOUT','handout']]){
  const raw={...draft,participations:[{candidateParticipationKey:'one',personName:'元人物',pcName:'元PC',handout:'元HO',role:'PL'}],unassignedValues:[{itemKey:'u',rawValue:'割当値',assignment:{type,candidateParticipationKey:'one'}}]};
  assert.equal(assignedData(raw).participations[0][field],'割当値');assert.notEqual(raw.participations[0][field],'割当値');
  assert.equal(assignedData({...raw,unassignedValues:[]}).participations[0][field],raw.participations[0][field]);
  assert.equal(assignedData({...raw,participations:[{...raw.participations[0],saveDecision:'OMIT'}]}).participations[0][field],raw.participations[0][field]);
  assert.equal(assignedData({...raw,unassignedValues:[...raw.unassignedValues,{itemKey:'other',rawValue:'競合値',assignment:{type,candidateParticipationKey:'one'}}]}).participations[0][field],raw.participations[0][field]);
  const edited=editAssignedField(raw,'one',field,'手編集');assert.equal(assignedData(edited).participations[0][field],'手編集');assert.equal(edited.unassignedValues[0].assignment.type,'UNASSIGNED');assert.equal(edited.unassignedValues[0].rawValue,'割当値');
 }
});

test('split moves assigned person PC and HO together and preserves unassigned and ignored items',()=>{
 for(const saveDecision of ['SAVE','OMIT','HOLD']){
  const person={candidateParticipationKey:'one',personName:'元人物',pcName:'元PC',handout:'元HO',role:'PL',saveDecision};
  const values=['PARTICIPATION_PERSON','PARTICIPATION_PC','PARTICIPATION_HANDOUT'].map((type,i)=>({itemKey:'u'+i,rawValue:['新人物','新PC','新HO'][i],assignment:{type,candidateParticipationKey:'one'}}));
  const untouched=[{itemKey:'free',rawValue:'未割当',assignment:{type:'UNASSIGNED',candidateParticipationKey:null}},{itemKey:'ignored',rawValue:'未保存',assignment:{type:'IGNORE',candidateParticipationKey:'one'}}];
  const parts=[{key:'a',data:{...draft,participations:[person],unassignedValues:[...values,...untouched]}},{key:'b',data:structuredClone(draft)}],before=structuredClone(parts);
  const moved=moveSplitParticipant(parts,0,'one');assert.deepEqual(parts,before);assert.deepEqual(moved[0].data.participations,[]);assert.deepEqual(moved[0].data.unassignedValues,untouched);assert.deepEqual(moved[1].data.unassignedValues,values);assert.equal(moved[1].data.participations[0].saveDecision,saveDecision);
  if(saveDecision!=='OMIT'){const effective=assignedData(moved[1].data).participations[0];assert.equal(effective.personName,'新人物');assert.equal(effective.pcName,'新PC');assert.equal(effective.handout,'新HO')}
  const returned=moveSplitParticipant(moved,1,'one');assert.deepEqual(returned[0].data.participations,[person]);assert.deepEqual(returned[1].data.unassignedValues,[]);assert.equal(returned[0].data.unassignedValues.length,5);
 }
});
