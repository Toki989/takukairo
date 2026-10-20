package jp.takukairo.importsession;
import java.util.*;
import static jp.takukairo.common.Json.*;
/** Group hypotheses only. Existing candidates are never automatically merged. */
public final class ImportBoundary {
 public record Member(long candidateId,long sourceId,String sourceFingerprint,Map<String,Object> data,Map<String,Object> inference) {}
 private record Prepared(Member member,String semantic,Set<String> people,Set<String> dates) {}
 private Prepared prepare(Member member){return new Prepared(member,semantic(member.data()),participations(member.data()),dates(member.data()));}
 public List<Map<String,Object>> evaluate(List<Member> group) {
  var prepared=group.stream().map(this::prepare).toList();var assessments=new ArrayList<Map<String,Object>>();
  for(int i=0;i<prepared.size();i++)for(int j=i+1;j<prepared.size();j++){var pair=assess(prepared.get(i),prepared.get(j));if(pair!=null)assessments.add(pair);}return assessments;
 }
 /** Only incident pairs are materialized for a candidate detail; no evidence limit. */
 public List<Map<String,Object>> evaluateFor(long candidateId,List<Member> group){
  var target=prepare(group.stream().filter(x->x.candidateId()==candidateId).findFirst().orElseThrow());var out=new ArrayList<Map<String,Object>>();
  for(var member:group)if(member.candidateId()!=candidateId){var other=prepare(member);var pair=member.candidateId()<candidateId?assess(other,target):assess(target,other);if(pair!=null)out.add(pair);}return out;
 }
 private Map<String,Object> assess(Prepared pa,Prepared pb){
  var a=pa.member();var b=pb.member();var evidence=new ArrayList<String>();String level="E0",conflict=null;
   if(a.sourceId()!=b.sourceId()&&(a.sourceFingerprint().equals(b.sourceFingerprint())||pa.semantic().equals(pb.semantic()))){return result(a,b,"E0","C2",List.of("COPY_SOURCE"));}
   Object scopeA=a.inference().get("scopeEvidenceId"),scopeB=b.inference().get("scopeEvidenceId");
   if(a.sourceId()==b.sourceId()&&scopeA!=null&&scopeB!=null&&!scopeA.equals(scopeB)){return result(a,b,"E0","C1",List.of("DISTINCT_EXPLICIT_SCOPES"));}
   if(nonblank(a.data().get("scenarioName"))&&Objects.equals(a.data().get("scenarioName"),b.data().get("scenarioName")))level="E1";
   // Repeated names/system/session/nearby dates are not independent table evidence.
   if(nonblank(a.data().get("recordingUrl"))&&Objects.equals(a.data().get("recordingUrl"),b.data().get("recordingUrl")))evidence.add("RECORDING_REFERENCE");
   var peopleA=pa.people();var peopleB=pb.people();
   if(!peopleA.isEmpty()&&peopleA.equals(peopleB))evidence.add("PARTICIPANT_PC_RECORD");
   var datesA=pa.dates();var datesB=pb.dates();
   if(!datesA.isEmpty()&&datesA.equals(datesB))evidence.add("EXACT_RECORDED_DATES");
   boolean contradicts=nonblank(a.data().get("scenarioName"))&&nonblank(b.data().get("scenarioName"))&&!Objects.equals(a.data().get("scenarioName"),b.data().get("scenarioName"));
   if(contradicts){if(!evidence.contains("RECORDING_REFERENCE")&&!evidence.contains("PARTICIPANT_PC_RECORD"))return null;conflict="C2";level="E0";}
   else if((evidence.contains("PARTICIPANT_PC_RECORD")&&(evidence.contains("RECORDING_REFERENCE")||evidence.contains("EXACT_RECORDED_DATES")))||(evidence.contains("RECORDING_REFERENCE")&&evidence.contains("EXACT_RECORDED_DATES"))){level="E2";conflict="C2";}
   else if("E1".equals(level))conflict="C2";
   return !level.equals("E0")||conflict!=null?result(a,b,level,conflict,evidence):null;
 }
 /** Every pair in the proposed group is checked directly, including counterevidence. */
 public Map<String,Object> evaluateGroup(List<Member> proposal){
  var prepared=proposal.stream().map(this::prepare).toList();var pairs=new ArrayList<Map<String,Object>>();var scopes=new HashMap<Long,String>();boolean separate=false;
  for(var member:proposal){var explicit=new ArrayList<>(list(member.inference().get("explicitScopes")));if(member.inference().get("scopeEvidenceId")!=null)explicit.add(map("sourceId",member.sourceId(),"scopeEvidenceId",member.inference().get("scopeEvidenceId")));for(var scope:explicit){long source=JsonId(scope.get("sourceId"));String value=text(scope.get("scopeEvidenceId"));String old=scopes.putIfAbsent(source,value);if(old!=null&&!Objects.equals(old,value))separate=true;}}
  boolean allDirect=proposal.size()>1;var counter=new ArrayList<Map<String,Object>>();
  for(int i=0;i<proposal.size();i++)for(int j=i+1;j<proposal.size();j++){long a=proposal.get(i).candidateId(),b=proposal.get(j).candidateId();var pair=assess(prepared.get(i),prepared.get(j));if(pair!=null)pairs.add(pair);else pair=map("candidateIds",List.of(a,b),"evidenceLevel","E0","conflict",null,"evidenceKinds",List.of());if("C1".equals(pair.get("conflict")))separate=true;if(!"E2".equals(pair.get("evidenceLevel"))){allDirect=false;counter.add(pair);}}
  return map("candidateIds",proposal.stream().map(Member::candidateId).toList(),"evidenceLevel",allDirect&&!separate?"E2":"E0","conflict",separate?"C1":proposal.size()>1?"C2":null,"pairAssessments",pairs,"counterEvidence",counter,"canMerge",!separate,"requiresSameTableConfirmation",proposal.size()>1,"automaticMerge",false);
 }
 private long JsonId(Object value){return ((Number)value).longValue();}
 private Map<String,Object> result(Member a,Member b,String level,String conflict,List<String> evidence){return map("candidateIds",List.of(a.candidateId(),b.candidateId()),"evidenceLevel",level,"conflict",conflict,"evidenceKinds",evidence,"requiresSameTableConfirmation",level.equals("E2"),"automaticMerge",false);}
 private String semantic(Map<String,Object> data){var normalized=object(write(data));for(var p:list(normalized.get("participations")))p.remove("candidateParticipationKey");for(var v:list(normalized.get("unassignedValues"))){v.remove("itemKey");obj(v.get("assignment")).remove("candidateParticipationKey");}return write(normalized);}
 private boolean nonblank(Object v){return v instanceof String s&&!s.isBlank();}
 private Set<String> participations(Map<String,Object> d){var out=new LinkedHashSet<String>();for(var p:list(d.get("participations")))if(nonblank(p.get("personName"))&&nonblank(p.get("pcName"))&&p.get("role")!=null)out.add(write(map("person",p.get("personName"),"pc",p.get("pcName"),"role",p.get("role"))));return out;}
 private Set<String> dates(Map<String,Object>d){var out=new LinkedHashSet<String>();list(d.get("tableDates")).forEach(x->{if(nonblank(x.get("playedOn")))out.add(text(x.get("playedOn")));});return out;}
}
