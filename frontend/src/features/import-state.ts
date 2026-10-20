import type {CandidateData,Resolution} from '../api/types';
export function resolutionName(r:Resolution):string {
 return r.decision==='CREATE_NEW'?String(r.draftData?.name??r.draftData?.displayName??''):String(r.createdEntityId??r.existingEntityId??'');
}
export function resolutionMismatch(r:Resolution|undefined,name:string,gameSystem?:string|null):boolean {
 return !!r&&r.decision==='CREATE_NEW'&&(resolutionName(r)!==name||(r.entityType==='SCENARIO'&&(r.draftData?.gameSystem??null)!==(gameSystem||null)));
}
export function candidatePatch(saved:{candidateData:CandidateData;confirmationStatus:string;registrationTarget:boolean},draft:CandidateData,checked:boolean,target:boolean,changes:Record<string,unknown>[],explicitChecked=false):Record<string,unknown> {
 const result:Record<string,unknown>={};
 if(JSON.stringify(saved.candidateData)!==JSON.stringify(draft))result.candidateData=draft;
 if((saved.confirmationStatus==='CHECKED')!==checked||checked&&explicitChecked)result.confirmationStatus=checked?'CHECKED':'UNCHECKED';
 if(saved.registrationTarget!==target)result.registrationTarget=target;
 if(changes.length)result.resolutionChanges=changes;
 return result;
}

export function pcOptionalDraft(part:CandidateData['participations'][number],current:Resolution|undefined,system:string|null):Record<string,unknown>{
 if(part.omittedFields?.includes('characterSheetUrl'))return current?.decision==='CREATE_NEW'&&!current.createdEntityId?{characterSheetUrl:null,systemSettings:Array.isArray(current.draftData?.systemSettings)?(current.draftData.systemSettings as Record<string,unknown>[]).map(s=>({...s,characterSheetUrl:null})):[]}:{};
 if(!part.characterSheetUrl)return {};
 if(part.sheetUrlTarget==='COMMON')return {characterSheetUrl:part.characterSheetUrl};
 return part.sheetUrlTarget==='SYSTEM'?{systemSettings:[{gameSystemName:system,canonicalSystemKey:null,profileKey:null,profileValues:{},characterSheetUrl:part.characterSheetUrl,displayOrder:1}]}:{};
}
