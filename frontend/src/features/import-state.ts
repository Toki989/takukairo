import type {CandidateData,Resolution} from '../api/types';
export function assignedData(raw:CandidateData):CandidateData {
 const d=structuredClone(raw),assigned=new Map<string,string>();
 for(const u of d.unassignedValues){const a=u.assignment,p=d.participations.find(p=>p.candidateParticipationKey===a.candidateParticipationKey);const field=a.type==='PARTICIPATION_PERSON'?'personName':a.type==='PARTICIPATION_PC'?'pcName':a.type==='PARTICIPATION_HANDOUT'?'handout':null;
  if(!field||!p||p.saveDecision==='OMIT'||p.omittedFields?.includes(field))continue;
  const key=p.candidateParticipationKey+':'+field;if(assigned.has(key)&&assigned.get(key)!==u.rawValue)return structuredClone(raw);assigned.set(key,u.rawValue);p[field]=u.rawValue;
 }return d;
}
export function editAssignedField(raw:CandidateData,key:string,field:'personName'|'pcName'|'handout',value:string):CandidateData {
 const type=field==='personName'?'PARTICIPATION_PERSON':field==='pcName'?'PARTICIPATION_PC':'PARTICIPATION_HANDOUT';
 return {...raw,participations:raw.participations.map(p=>p.candidateParticipationKey===key?{...p,[field]:value}:p),unassignedValues:raw.unassignedValues.map(u=>u.assignment.type===type&&u.assignment.candidateParticipationKey===key?{...u,assignment:{type:'UNASSIGNED',candidateParticipationKey:null}}:u)};
}
export function moveSplitParticipant<T extends {data:CandidateData}>(parts:T[],from:number,key:string):T[] {
 if(parts.length<2)return parts;
 const source=parts[from],participant=source.data.participations.find(p=>p.candidateParticipationKey===key);
 if(!participant)return parts;
 const to=(from+1)%parts.length;
 const follows=(value:CandidateData['unassignedValues'][number])=>value.assignment.type.startsWith('PARTICIPATION_')&&value.assignment.candidateParticipationKey===key;
 const values=source.data.unassignedValues.filter(follows);
 return parts.map((part,index)=>index===from?{...part,data:{...part.data,participations:part.data.participations.filter(p=>p.candidateParticipationKey!==key),unassignedValues:part.data.unassignedValues.filter(v=>!follows(v))}}:index===to?{...part,data:{...part.data,participations:[...part.data.participations,participant],unassignedValues:[...part.data.unassignedValues,...values]}}:part);
}
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
