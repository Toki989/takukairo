import {test} from 'node:test';
import assert from 'node:assert/strict';
import {candidatePatch,resolutionMismatch,resolutionName} from '../src/features/import-state.ts';
const draft={scenarioName:'A',gameSystemName:null,tableName:null,tableDates:[],recordingUrl:null,participations:[],unassignedValues:[]};
const saved={candidateData:draft,confirmationStatus:'CHECKED',registrationTarget:true};
test('unchanged PATCH omits confirmation, draft and target',()=>assert.deepEqual(candidatePatch(saved,draft,true,true,[]),{}));
test('edit clears checked without automatically resending old checked',()=>{const patch=candidatePatch(saved,{...draft,scenarioName:'B'},false,true,[]);assert.equal(patch.confirmationStatus,'UNCHECKED');assert.equal(patch.candidateData.scenarioName,'B');assert.equal('registrationTarget'in patch,false)});
test('resolution mismatch shows saved draft and never mutates shared resolution',()=>{const r={entityType:'SCENARIO',decision:'CREATE_NEW',draftData:{name:'saved-name',gameSystem:null}};assert.equal(resolutionName(r),'saved-name');assert.equal(resolutionMismatch(r,'edited-name'),true);assert.equal(r.draftData.name,'saved-name');assert.equal(resolutionMismatch({...r,draftData:{name:'edited-name',gameSystem:'CoC'}},'edited-name','Other'),true)});
test('explicit confirm remains a distinct operation from autosave',()=>assert.deepEqual(candidatePatch({...saved,confirmationStatus:'UNCHECKED'},draft,true,true,[]),{confirmationStatus:'CHECKED'}));

test('same PATCH includes CHECKED only after explicit reconfirmation even when baseline was checked',()=>{const edited={...draft,tableName:'final'};assert.equal(candidatePatch(saved,edited,true,true,[],true).confirmationStatus,'CHECKED');assert.equal('confirmationStatus'in candidatePatch(saved,edited,true,true,[]),false)});
