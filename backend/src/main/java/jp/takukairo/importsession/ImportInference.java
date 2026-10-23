package jp.takukairo.importsession;
import java.util.*;
import java.time.LocalDate;
import java.util.regex.*;
import static jp.takukairo.common.Json.*;
import static jp.takukairo.importsession.ImportStructure.*;
/** One inference path for all formats. No string-identity or transitive table union. */
public final class ImportInference {
 private static final Set<String> PARTICIPANT_FIELDS=Set.of("pc","role","handout","displayQuote","spotlightType","characterSheetUrl","stateSan","stateHp","stateMp","stateGrowth","stateOutcome","stateAftereffects");
 private record Context(String field,String value,Block block) {}
 private static final class Draft {
  final Map<String,Object> data=map("scenarioName","","gameSystemName",null,"tableName",null,"tableDates",new ArrayList<>(),"recordingUrl",null,"participations",new ArrayList<>(),"unassignedValues",new ArrayList<>());
  final List<Map<String,Object>> traces=new ArrayList<>(),relations=new ArrayList<>(),conflicts=new ArrayList<>();
  final Set<String> usedContext=new LinkedHashSet<>();
  final Map<String,Range> pcRanges=new HashMap<>();final Map<String,Block> pcBlocks=new HashMap<>();final Map<String,String> pcEvidence=new HashMap<>();
  final Map<String,List<String>> recordEvidence=new HashMap<>();final Map<String,String> personEvidence=new HashMap<>();
  final Map<String,Boolean> recordCompleted=new HashMap<>(),recordExcluded=new HashMap<>();
  final List<String> localContexts=new ArrayList<>();boolean recordMode,baseCompleted,baseExcluded,quotedEvidence;List<String> baseContexts=List.of(),baseEvidence=List.of();
  int recordStart=Integer.MAX_VALUE,recordEnd;String scopeEvidenceId;boolean completed,explicitTable,contextExcluded;int start=Integer.MAX_VALUE,end;final StringBuilder excerpt=new StringBuilder();Map<String,Object> participant;
  void source(Block b){recordStart=Math.min(recordStart,b.range().startOffset());recordEnd=Math.max(recordEnd,b.range().endOffset());start=Math.min(start,b.range().lineStart());end=Math.max(end,b.range().lineEnd());if(!excerpt.isEmpty())excerpt.append('\n');excerpt.append(b.range().excerpt());}
  boolean populated(){return !traces.isEmpty()||!list(data.get("participations")).isEmpty()||!list(data.get("unassignedValues")).isEmpty();}
 }
 public List<ImportParser.Parsed> infer(Document doc) {
  var results=new ArrayList<ImportParser.Parsed>();var contexts=new TreeMap<Integer,List<Context>>();var completion=new TreeMap<Integer,Boolean>();var explicit=new TreeMap<Integer,Boolean>();Draft current=new Draft(),tableContext=null;Kind previous=null;
  for(int blockIndex=0;blockIndex<doc.blocks().size();blockIndex++) {var b=doc.blocks().get(blockIndex);
   if(b.kind()==Kind.UNPARSED){emit(current,results,doc.format());current=new Draft();current.quotedEvidence=b.quoted();current.source(b);unknown(current,b.range().excerpt(),b.range(),b);emit(current,results,doc.format());results.getLast().inference().put("unparsedReason","表の列対応を保証できない構造です。原文を確認し、参加記録ごとに候補化するか明示除外してください。");current=new Draft();tableContext=null;previous=b.kind();continue;}
   if(b.quoted()){if(!current.populated())inherit(current,contexts,completion,explicit);current.quotedEvidence=true;current.source(b);if(b.text().codePoints().anyMatch(Character::isLetterOrDigit))unknown(current,"引用: "+b.text(),b.range(),b);else current.traces.add(b.range().trace("/inference/quoted",0,b.id()+":quoted","DIRECT",List.of()));continue;}
   if(b.kind()==Kind.HEADING){tableContext=null;previous=b.kind();emit(current,results,doc.format());current=new Draft();contexts.tailMap(b.level()).clear();completion.tailMap(b.level()).clear();explicit.tailMap(b.level()).clear();var pair=pair(b.text());String key=field(pair[0]);
    if(Set.of("scenarioName","gameSystemName","tableName").contains(key)&&!pair[1].isBlank()){contexts.put(b.level(),new ArrayList<>(List.of(new Context(key,pair[1],b))));if(key.equals("tableName"))explicit.put(b.level(),true);}else if(!completed(b.text())&&!harmlessHeading(b.text())){contexts.put(b.level(),new ArrayList<>(List.of(new Context("unknown",b.text(),b))));}
    boolean statusHeading=key.equals("context")||key.equals("unknown")&&pair[1].isEmpty()&&(completed(b.text())||excludedContext(b.text()));if(statusHeading){completion.put(b.level(),completed(b.text()));contexts.computeIfAbsent(b.level(),k->new ArrayList<>()).add(new Context("context",b.text(),b));}continue;
   }
   if(b.kind()==Kind.BLANK){int next=blockIndex+1;while(next<doc.blocks().size()&&doc.blocks().get(next).kind()==Kind.BLANK)next++;if(previous!=Kind.RECORD&&list(current.data.get("participations")).isEmpty()&&next<doc.blocks().size()&&doc.blocks().get(next).kind()==Kind.RECORD)continue;tableContext=null;previous=b.kind();if(!current.explicitTable){emit(current,results,doc.format());current=new Draft();}continue;}
   if(b.kind()==Kind.PARAGRAPH&&b.text().codePoints().noneMatch(Character::isLetterOrDigit))continue;
   if(b.kind()==Kind.RECORD&&!current.explicitTable){
    if(tableContext==null&&list(current.data.get("participations")).isEmpty()&&current.populated())tableContext=current;
    boolean sharedOnly=tableContext==current;if(!sharedOnly)emit(current,results,doc.format());current=new Draft();
    if(tableContext!=null){inherit(current,contexts,completion,explicit);current.completed=tableContext.completed;current.contextExcluded=tableContext.contextExcluded;current.usedContext.addAll(tableContext.usedContext);for(String field:List.of("scenarioName","gameSystemName","tableName","authorName","sourceUrl","recordingUrl"))if(tableContext.data.get(field)!=null)current.data.put(field,tableContext.data.get(field));for(var t:tableContext.traces)if("/inference/context".equals(t.get("fieldPath"))||text(t.get("fieldPath")).startsWith("/table/")){var inherited=new LinkedHashMap<>(t);inherited.put("derivation","INHERITED");inherited.put("dependsOn",List.of(t.get("evidenceId")));current.traces.add(inherited);}if(sharedOnly){list(current.data.get("unassignedValues")).addAll(list(tableContext.data.get("unassignedValues")));for(var t:tableContext.traces)if(text(t.get("fieldPath")).startsWith("/unassignedValues/"))current.traces.add(new LinkedHashMap<>(t));}}
   }
   if(b.kind()!=Kind.RECORD){tableContext=null;if(previous==Kind.RECORD&&!current.explicitTable){emit(current,results,doc.format());current=new Draft();}}
   previous=b.kind();
   if(!current.populated())inherit(current,contexts,completion,explicit);
   if(b.kind()==Kind.PARAGRAPH){var entry=pair(b.text());if(field(entry[0]).equals("tableName")&&!entry[1].isBlank()&&current.data.get("tableName")!=null&&!Objects.equals(current.data.get("tableName"),entry[1])&&!current.explicitTable&&!list(current.data.get("participations")).isEmpty()){
    // A repeated label after an activity is a boundary hypothesis, never proof of C1.
    var prior=current;var hypothesis=map("kind","C2","fieldPath","/inference/boundary","values",List.of(prior.data.get("tableName"),entry[1]),"reasonCode","REPEATED_TABLE_AFTER_ACTIVITY","evidenceId",b.id());prior.conflicts.add(hypothesis);prior.traces.add(b.range().trace("/inference/boundary",0,b.id(),"UNRESOLVED",List.of()));emit(prior,results,doc.format());current=new Draft();inherit(current,contexts,completion,explicit);
    for(String shared:List.of("scenarioName","gameSystemName","authorName","sourceUrl"))if(prior.data.get(shared)!=null){current.data.put(shared,prior.data.get(shared));for(var trace:prior.traces)if(("/table/"+shared).equals(trace.get("fieldPath"))){var inherited=new LinkedHashMap<>(trace);inherited.put("derivation","INHERITED");inherited.put("dependsOn",List.of(trace.get("evidenceId")));current.traces.add(inherited);}}
    // A paragraph label cannot inherit the previous explicit scope as a proven same-table link.
    current.scopeEvidenceId=null;current.explicitTable=false;current.conflicts.add(new LinkedHashMap<>(hypothesis));current.traces.add(b.range().trace("/inference/boundary",0,b.id(),"UNRESOLVED",List.of()));tableContext=null;
   }}
   current.source(b);
   if(b.kind()==Kind.RECORD){current.participant=null;record(current,b);for(var trace:current.traces)for(var cell:b.cells())if(cell.labelRange()!=null&&Objects.equals(trace.get("startOffset"),cell.range().startOffset())){trace.put("labelLocation",cell.labelRange().trace("/structure/columnLabel",0,"header:"+cell.labelRange().startOffset(),"DIRECT",List.of()));trace.put("dependsOnLabel", "header:"+cell.labelRange().startOffset());}}
   else {
    var pair=pair(b.text());String key=field(pair[0]);
    if(key.equals("scenarioName")&&!Objects.equals(current.data.get("scenarioName"),"")&&!Objects.equals(current.data.get("scenarioName"),pair[1])&&!current.explicitTable){emit(current,results,doc.format());current=new Draft();inherit(current,contexts,completion,explicit);current.source(b);}
    if((actualContext(b.text())||excludedContext(b.text()))&&pair[1].isEmpty()&&!b.text().matches(".*[:：].*")){accept(current,"context",b.text(),b.range(),b,b.text());}
    else inline(current,b);
   }
  }
  emit(current,results,doc.format());
  // A meaningful field heading without any body must not disappear from accounting.
  var covered=new HashSet<Object>();results.forEach(p->p.traces().forEach(t->{int at=((Number)t.get("startOffset")).intValue();boolean anchored=!list(p.data().get("participations")).isEmpty()||!list(p.data().get("tableDates")).isEmpty()||List.of("scenarioName","tableName","recordingUrl").stream().anyMatch(key->p.data().get(key) instanceof String s&&!s.isBlank());if(anchored||!text(t.get("fieldPath")).startsWith("/unassignedValues/")||at>=((Number)p.inference().get("recordStartOffset")).intValue()&&at<((Number)p.inference().get("recordEndOffset")).intValue())covered.add(at);}));
  for(var b:doc.blocks())if(b.kind()==Kind.HEADING&&!b.quoted()&&!harmlessHeading(b.text())&&!covered.contains(b.range().startOffset())){var d=new Draft();d.source(b);var pair=pair(b.text());accept(d,field(pair[0]),field(pair[0]).equals("unknown")?b.text():pair[1],b.range(),b,b.text());emit(d,results,doc.format());}
  results.sort(Comparator.comparingInt(p->((Number)p.inference().get("recordStartOffset")).intValue()));return assemble(results);
 }
 private List<ImportParser.Parsed> assemble(List<ImportParser.Parsed> pieces) {
  var result=new ArrayList<ImportParser.Parsed>();var explicit=new LinkedHashMap<String,Integer>();
  for(var piece:pieces){String scope=text(piece.inference().get("scopeEvidenceId"));if(scope==null||!explicit.containsKey(scope)){if(scope!=null)explicit.put(scope,result.size());result.add(piece);continue;}
   int index=explicit.get(scope);var first=result.get(index);var data=object(write(first.data()));var traces=new ArrayList<>(first.traces());traces.addAll(piece.traces());var metadata=object(write(first.inference()));var conflicts=new ArrayList<>(list(metadata.get("conflicts")));conflicts.addAll(list(piece.inference().get("conflicts")));
   for(String field:List.of("scenarioName","gameSystemName","tableName","recordingUrl","authorName","sourceUrl")){Object a=data.get(field),b=piece.data().get(field);if(a==null||a.equals(""))data.put(field,b);else if(b!=null&&!b.equals("")&&!Objects.equals(a,b)){String item="u-"+UUID.randomUUID();list(data.get("unassignedValues")).add(map("itemKey",item,"rawValue",field+": "+b,"assignment",map("type","UNASSIGNED","candidateParticipationKey",null)));conflicts.add(map("kind","C3","fieldPath","/table/"+field,"values",List.of(a,b),"scopeEvidenceId",scope));}}
   for(String field:List.of("tableDates","participations","unassignedValues"))list(data.get(field)).addAll(list(piece.data().get(field)));var relations=new ArrayList<>(list(metadata.get("relations")));relations.addAll(list(piece.inference().get("relations")));metadata.put("relations",relations);metadata.put("conflicts",conflicts);result.set(index,new ImportParser.Parsed(data,Math.min(first.lineStart(),piece.lineStart()),Math.max(first.lineEnd(),piece.lineEnd()),first.excerpt()+"\n"+piece.excerpt(),traces,metadata));
  }
  return result;
 }
 private void inherit(Draft d,TreeMap<Integer,List<Context>> contexts,TreeMap<Integer,Boolean> completion,TreeMap<Integer,Boolean> explicit) {
  d.completed=!completion.isEmpty()&&completion.lastEntry().getValue();d.contextExcluded=!completion.isEmpty()&&!completion.lastEntry().getValue();d.explicitTable=explicit.values().stream().anyMatch(Boolean.TRUE::equals);
  for(var values:contexts.values())for(var c:values){if(c.field().equals("context")){String id=c.block().id()+":context";d.usedContext.add(id);d.traces.add(c.block().range().trace("/inference/context",0,id,"INHERITED",List.of(id)));continue;}if(c.field().equals("unknown")){unknown(d,c.value(),c.block().range(),c.block());continue;}d.data.put(c.field(),c.value());if(c.field().equals("tableName"))d.scopeEvidenceId=c.block().id();String id=c.block().id()+":"+c.field();d.usedContext.add(id);d.traces.add(c.block().range().trace("/table/"+c.field(),0,id,"INHERITED",List.of(id)));}
 }
 private void record(Draft d,Block b) {
  var outerEvidence=new LinkedHashSet<>(d.usedContext);boolean outerCompleted=d.completed,outerExcluded=d.contextExcluded;int first=list(d.data.get("participations")).size();
  var outerContexts=new ArrayList<>(d.localContexts);d.localContexts.clear();d.recordMode=true;
  // Interpret row context before roles, but do not let a row exception leak to other records.
  for(var c:b.cells())if(field(c.label()).equals("context")&&hasValue(c))accept(d,"context",c.value(),c.range(),b,c.label()+": "+c.value());
  for(var c:b.cells())if(!Set.of("person","KP","PL","SKP","context","progress").contains(field(c.label()))&&!PARTICIPANT_FIELDS.contains(field(c.label())))if(hasValue(c))accept(d,field(c.label()),c.value(),c.range(),b,c.label()+": "+c.value());
  for(var c:b.cells())if(Set.of("person","KP","PL","SKP").contains(field(c.label()))&&hasValue(c))accept(d,field(c.label()),c.value(),c.range(),b,c.label()+": "+c.value());
  var rowParts=list(d.data.get("participations")).subList(first,list(d.data.get("participations")).size());if(rowParts.size()>1){var eligible=rowParts.stream().filter(part->!"KP".equals(part.get("role"))).toList();d.participant=eligible.size()==1?eligible.getFirst():null;}
  for(var c:b.cells())if(PARTICIPANT_FIELDS.contains(field(c.label()))&&hasValue(c))accept(d,field(c.label()),c.value(),c.range(),b,c.label()+": "+c.value());
  for(var c:b.cells())if(field(c.label()).equals("progress")&&hasValue(c))accept(d,"progress",c.value(),c.range(),b,c.label()+": "+c.value());
  var parts=list(d.data.get("participations"));for(int i=first;i<parts.size();i++){String key=text(parts.get(i).get("candidateParticipationKey"));d.recordCompleted.put(key,d.completed);d.recordExcluded.put(key,d.contextExcluded);d.recordEvidence.put(key,new ArrayList<>(d.usedContext));}
  d.completed=outerCompleted;d.contextExcluded=outerExcluded;d.usedContext.clear();d.usedContext.addAll(outerEvidence);d.localContexts.clear();d.localContexts.addAll(outerContexts);d.recordMode=false;d.participant=null;
 }
 private boolean hasValue(Cell c){return field(c.label()).equals("displayQuote")?!c.value().isEmpty():!c.value().isBlank();}
 private void accept(Draft d,String key,String value,Range range,Block b,String original) {
  String evidence=b.id()+":"+key+":"+range.startOffset();
  if(!key.equals("unknown")&&(value.isEmpty()||!key.equals("displayQuote")&&value.isBlank())){d.traces.add(range.trace("/inference/missing/"+key,b.logicalRecord(),evidence,"DIRECT",List.of()));return;}
  if(Set.of("scenarioName","gameSystemName","tableName","recordingUrl","authorName","sourceUrl").contains(key)) {
   Object old=d.data.get(key);if(old!=null&&!old.equals("")&&!Objects.equals(old,value)&&(key.equals("tableName")&&d.explicitTable||d.traces.stream().anyMatch(t->Objects.equals(t.get("fieldPath"),"/table/"+key)&&!Objects.equals(t.get("derivation"),"INHERITED")))){d.conflicts.add(map("kind","C3","fieldPath","/table/"+key,"values",List.of(old,value),"evidenceId",evidence));unknown(d,original,range,b);}
   else d.data.put(key,value);
   d.traces.add(range.trace("/table/"+key,b.logicalRecord(),evidence,"DIRECT",List.of()));return;
  }
  if(key.equals("date")){try{list(d.data.get("tableDates")).add(map("playedOn",recordedDate(value).toString()));d.traces.add(range.trace("/table/tableDates",b.logicalRecord(),evidence,"DIRECT",List.of()));}catch(Exception invalid){unknown(d,original,range,b);}return;}
  if(Set.of("KP","PL","SKP","person").contains(key)) {
   if(!d.recordMode){if(d.participant==null){d.baseCompleted=d.completed;d.baseExcluded=d.contextExcluded;d.baseContexts=new ArrayList<>(d.localContexts);d.baseEvidence=new ArrayList<>(d.usedContext);}else{d.completed=d.baseCompleted;d.contextExcluded=d.baseExcluded;d.localContexts.clear();d.localContexts.addAll(d.baseContexts);d.usedContext.clear();d.usedContext.addAll(d.baseEvidence);}}
   String role=Set.of("KP","PL").contains(key)?key:null;String name=value,pc=null;
   // Only explicit PC markers establish a value boundary; spaces/slashes are names.
   var marker=Pattern.compile("^(.+?)\\s*[（(]PC[:：]\\s*(.+)[）)]$",Pattern.CASE_INSENSITIVE).matcher(value);
   if(marker.matches()){name=marker.group(1).strip();pc=marker.group(2);}
   var p=map("candidateParticipationKey","p-"+UUID.randomUUID(),"personName",name,"role",role,"pcName",pc,"handout",null);list(d.data.get("participations")).add(p);d.participant=p;d.personEvidence.put(text(p.get("candidateParticipationKey")),evidence);
   d.traces.add(range.trace("/participations/"+p.get("candidateParticipationKey")+"/personName",b.logicalRecord(),evidence,"DIRECT",List.of()));
   if(role!=null)d.traces.add(range.trace("/participations/"+p.get("candidateParticipationKey")+"/role",b.logicalRecord(),evidence,"DIRECT",List.of()));
   if(key.equals("SKP")){d.relations.add(map("candidateParticipationKey",p.get("candidateParticipationKey"),"state","UNDETERMINED","originalRole","SKP","evidenceIds",List.of(evidence)));d.traces.add(range.trace("/participations/"+p.get("candidateParticipationKey")+"/role",b.logicalRecord(),evidence,"UNRESOLVED",List.of()));}
   snapshotContext(d,p);
   if(pc!=null){String pcId=evidence+":pc";d.traces.add(range.trace("/participations/"+p.get("candidateParticipationKey")+"/pcName",b.logicalRecord(),pcId,"DIRECT",List.of()));inferRole(d,p,range,b,pcId);}return;
  }
  if(PARTICIPANT_FIELDS.contains(key)&&!Set.of("pc","role","handout").contains(key)){
   if(d.participant==null){unknown(d,original,range,b);return;}var p=d.participant;String field=key;
   if(key.startsWith("state")){var state=obj(p.get("endPcState"));p.put("endPcState",state);if(Set.of("stateSan","stateHp","stateMp").contains(key)){var values=obj(state.get("statusValues"));state.put("statusValues",values);field=key.substring(5).toLowerCase(Locale.ROOT);Object parsed;try{parsed=Long.valueOf(value);}catch(Exception invalid){parsed=value;}if(values.containsKey(field)&&!Objects.equals(values.get(field),parsed)){unknown(d,original,range,b);return;}values.put(field,parsed);field="endPcState/statusValues/"+field;}else{String target=key.substring(5);target=Character.toLowerCase(target.charAt(0))+target.substring(1);if(state.get(target)!=null&&!Objects.equals(state.get(target),value)){unknown(d,original,range,b);return;}state.put(target,value);field="endPcState/"+target;}}
   else{if(p.get(key)!=null&&!Objects.equals(p.get(key),value)){unknown(d,original,range,b);return;}p.put(key,value);}
   d.traces.add(range.trace("/participations/"+p.get("candidateParticipationKey")+"/"+field,b.logicalRecord(),evidence,"DIRECT",List.of()));return;
  }
  if(Set.of("pc","role","handout").contains(key)) {
   if(d.participant==null){unknown(d,value,range,b);return;}var p=d.participant;String field=key.equals("pc")?"pcName":key.equals("handout")?"handout":"role";
   if(key.equals("role")&&!Set.of("KP","PL").contains(value)){p.put("role",null);d.relations.add(map("candidateParticipationKey",p.get("candidateParticipationKey"),"state","UNDETERMINED","originalRole",value,"evidenceIds",List.of(evidence)));d.traces.add(range.trace("/participations/"+p.get("candidateParticipationKey")+"/role",b.logicalRecord(),evidence,"UNRESOLVED",List.of()));if(value.matches("(?is).*\\bKP\\b.*\\bPL\\b.*|.*\\bPL\\b.*\\bKP\\b.*")){var relation=d.relations.getLast();relation.put("state","ALTERNATIVES");relation.put("candidateRoles",List.of("KP","PL"));relation.put("reason","原文にKPとPLの両候補があるため明示選択が必要");}else if(!value.equals("SKP"))unknown(d,original,range,b);return;}
   if(p.get(field)!=null&&!Objects.equals(p.get(field),value)){d.conflicts.add(map("kind","C3","fieldPath","/participations/"+p.get("candidateParticipationKey")+"/"+field,"values",List.of(p.get(field),value),"evidenceId",evidence));unknown(d,original,range,b);return;}
   p.put(field,value);d.traces.add(range.trace("/participations/"+p.get("candidateParticipationKey")+"/"+field,b.logicalRecord(),evidence,"DIRECT",List.of()));if(key.equals("pc"))inferRole(d,p,range,b,evidence);return;
  }
  if(key.equals("progress")) {
   boolean local=d.recordMode||d.participant==null;boolean actual=local?d.completed:d.baseCompleted,excluded=local?d.contextExcluded:d.baseExcluded;
   if(actual&&!excluded&&String.valueOf(d.data.get("gameSystemName")).matches("(?i).*(クトゥルフ|CoC).*")){accept(d,"KP",value,range,b,original);String part=text(d.participant.get("candidateParticipationKey"));var deps=new ArrayList<String>(d.usedContext);deps.add(evidence);d.relations.add(map("candidateParticipationKey",part,"state","PROVISIONAL","role","KP","reason","CoC実施卓のゲーム進行担当","evidenceIds",deps));for(var trace:d.traces)if(Objects.equals(trace.get("fieldPath"),"/participations/"+part+"/role")){trace.put("derivation","INFERRED");trace.put("dependsOn",deps);}return;}
   unknown(d,original,range,b);return;
  }
  if(key.equals("context")){
   if(d.localContexts.isEmpty()){d.completed=false;d.contextExcluded=false;d.usedContext.removeIf(id->id.contains(":context"));}
   d.localContexts.add(value);d.completed|=actualContext(value);d.contextExcluded|=!actualContext(value);d.usedContext.add(evidence);d.traces.add(range.trace("/inference/context",b.logicalRecord(),evidence,"DIRECT",List.of()));
   if(d.completed&&d.contextExcluded){String scope=d.recordMode?b.id():"paragraph";Object participant=d.participant==null?null:d.participant.get("candidateParticipationKey");d.conflicts.removeIf(x->"/inference/context".equals(x.get("fieldPath"))&&Objects.equals(x.get("contextScope"),scope)&&Objects.equals(x.get("participantKey"),participant));d.conflicts.add(map("kind","C3","fieldPath","/inference/context","contextScope",scope,"participantKey",participant,"values",new ArrayList<>(d.localContexts),"evidenceIds",new ArrayList<>(d.usedContext)));}
   if(d.participant!=null)snapshotContext(d,d.participant);if(!completed(value))unknown(d,original,range,b);return;
  }
  unknown(d,original,range,b);
 }
 private void snapshotContext(Draft d,Map<String,Object> p){String key=text(p.get("candidateParticipationKey"));d.recordCompleted.put(key,d.completed);d.recordExcluded.put(key,d.contextExcluded);d.recordEvidence.put(key,new ArrayList<>(d.usedContext));}
 private boolean onlyContext(Draft d){
  if(!list(d.data.get("participations")).isEmpty()||!list(d.data.get("tableDates")).isEmpty())return false;
  for(String field:List.of("scenarioName","gameSystemName","tableName","recordingUrl","authorName","sourceUrl"))if(d.data.get(field) instanceof String s&&!s.isBlank())return false;
  var starts=new HashSet<Object>();d.traces.stream().filter(t->"/inference/context".equals(t.get("fieldPath"))).forEach(t->starts.add(t.get("startOffset")));return !starts.isEmpty()&&d.traces.stream().allMatch(t->starts.contains(t.get("startOffset")));
 }
 private void inferRole(Draft d,Map<String,Object> p,Range r,Block b,String evidence) {
  String key=text(p.get("candidateParticipationKey"));d.pcRanges.put(key,r);d.pcBlocks.put(key,b);d.pcEvidence.put(key,evidence);
 }
 private void finalizeRoles(Draft d) {
  for(var p:list(d.data.get("participations"))){String key=text(p.get("candidateParticipationKey"));boolean excluded=d.relations.stream().anyMatch(x->Objects.equals(x.get("candidateParticipationKey"),key)&&x.containsKey("originalRole"));
   if(d.recordExcluded.getOrDefault(key,d.contextExcluded)){
    var contextEvidence=d.recordEvidence.getOrDefault(key,new ArrayList<>(d.usedContext));for(var relation:d.relations)if(Objects.equals(relation.get("candidateParticipationKey"),key))relation.put("eligibleActualParticipation",false);
    if(p.get("role")!=null){String observed=text(p.get("role"));p.put("role",null);for(var relation:d.relations)if(Objects.equals(relation.get("candidateParticipationKey"),key)&&"PROVISIONAL".equals(relation.get("state"))){relation.put("state","UNDETERMINED");relation.put("reason","予定・引用・担当等の文脈は実参加Roleを確定しない");}d.relations.add(map("candidateParticipationKey",key,"state","UNDETERMINED","originalRole",observed,"reason","予定・引用・担当等の文脈は実参加Roleを確定しない","evidenceIds",contextEvidence));}
    for(var trace:d.traces)if(("/participations/"+key+"/role").equals(trace.get("fieldPath"))){trace.put("derivation","UNRESOLVED");trace.put("dependsOn",contextEvidence);}continue;
   }
   if(p.get("role")==null&&p.get("pcName")!=null&&d.recordCompleted.getOrDefault(key,d.completed)&&!excluded&&d.pcRanges.containsKey(key)){p.put("role","PL");var deps=new ArrayList<String>(d.recordEvidence.getOrDefault(key,new ArrayList<>(d.usedContext)));if(d.personEvidence.get(key)!=null)deps.add(d.personEvidence.get(key));deps.add(d.pcEvidence.get(key));d.relations.add(map("candidateParticipationKey",key,"state","PROVISIONAL","role","PL","reason","実施済み参加者と使用PCの明示対応","evidenceIds",deps));d.traces.add(d.pcRanges.get(key).trace("/participations/"+key+"/role",d.pcBlocks.get(key).logicalRecord(),d.pcEvidence.get(key)+":role","INFERRED",deps));}
  }
 }
 private void resolveKnownSkp(Draft d){boolean main=list(d.data.get("participations")).stream().anyMatch(p->"KP".equals(p.get("role"))&&d.relations.stream().noneMatch(r->Objects.equals(r.get("candidateParticipationKey"),p.get("candidateParticipationKey"))&&"SKP".equals(r.get("originalRole"))));if(!main)return;var skpKeys=new HashSet<String>();for(var r:d.relations)if("SKP".equals(r.get("originalRole"))&&!Boolean.FALSE.equals(r.get("eligibleActualParticipation"))){r.put("hasMainKp",true);r.put("state","PROVISIONAL");skpKeys.add(text(r.get("candidateParticipationKey")));}var parts=list(d.data.get("participations"));for(var p:parts)if(skpKeys.contains(text(p.get("candidateParticipationKey"))))p.put("role","KP");parts.sort(Comparator.comparingInt(p->skpKeys.contains(text(p.get("candidateParticipationKey")))?1:0));}
 private boolean excludedContext(String s){return s.matches("(?is).*(予定|候補|未実施|未完了|未プレイ|未通過|未確認|不明|不確実|かもしれ|かどうか|募集|連絡|日程調整|主催|作者|投稿者|引用|NPC|(?:実施済み?|完了|プレイ済み?|通過済み?)(?:では(?:あり)?ません|で(?:は)?ない|ではなく|していない)|[?？]|\\b(?:planned|candidate|tentative|quote|uncompleted|unplayed|unknown|uncertain|recruiting|unconfirmed)\\b|\\b(?:not|never)\\s+(?:yet\\s+)?(?:completed|played)\\b).*")||s.strip().matches("(?s)^(?:\".*\"|“.*”|「.*」|『.*』)$");}
 private boolean actualContext(String s){return !excludedContext(s)&&s.matches("(?is).*(実施済|プレイ済|通過済|\\bcompleted\\b|\\bplayed\\b).*");}
 private boolean completed(String s){return actualContext(s)&&!excludedContext(s);}
 private boolean harmlessHeading(String s){return s.codePoints().noneMatch(Character::isLetterOrDigit)||s.strip().matches("(?i)(?:概要|目次|参加者|参加者一覧|その他|メモ|Overview|Contents)");}
 private void inline(Draft d,Block b){
  var segments=roleSegments(b.text());if(segments.size()>1){for(String segment:segments)inlineFields(d,new Block(b.id(),b.kind(),b.level(),b.parentId(),segment,b.cells(),b.range(),b.logicalRecord(),b.quoted()));return;}inlineFields(d,b);
 }
 private List<String> roleSegments(String text){
  var out=new ArrayList<String>();int start=0,depth=0;boolean asciiQuote=false;
  for(int i=0;i<text.length();i++){char ch=text.charAt(i);if(ch=='"')asciiQuote=!asciiQuote;if("「『（(“".indexOf(ch)>=0)depth++;if("」』）)”".indexOf(ch)>=0)depth=Math.max(0,depth-1);
   if(depth==0&&!asciiQuote&&"、,／/".indexOf(ch)>=0&&text.substring(i+1).matches("(?is)^\\s*(?:KP|SKP|PL)\\s*[:：].*")){out.add(text.substring(start,i));start=i+1;}
   // Everything after a Quote label may be dialogue containing role labels.
   if(ch==':'||ch=='：'){int labelStart=start;for(int j=i-1;j>=start;j--)if("／/".indexOf(text.charAt(j))>=0){labelStart=j+1;break;}String label=text.substring(labelStart,i).strip();if(field(label).equals("displayQuote"))break;}
  }out.add(text.substring(start));return out;
 }
 private void inlineFields(Draft d,Block b){
  String[] segments=b.text().split("(?i)[／/]\\s*(?=(?:PC|HO|セリフ|台詞|Quote|終了SAN|終了HP|終了MP|Spotlight)[:：])",-1);int quote=-1;
  for(int i=0;i<segments.length;i++)if(field(pair(segments[i])[0]).equals("displayQuote")){quote=i;break;}
  if(quote>=0&&quote<segments.length-1){
   // Neither punctuation nor a Quote label alone disambiguates dialogue from inline attributes.
   int at=0;for(int i=0;i<quote;i++){at+=segments[i].length();var delimiter=Pattern.compile("[／/]\\s*").matcher(b.text());delimiter.region(at,b.text().length());if(delimiter.lookingAt())at=delimiter.end();}
   String original=b.text().substring(at),whole=pair(original)[1];boolean inlineContext=quote>=2;var alternatives=new ArrayList<Map<String,Object>>();
   for(int i=quote;i<segments.length;i++){var entry=pair(segments[i]);alternatives.add(map("field",field(entry[0]),"value",entry[1]));}
   for(int i=0;i<quote;i++){var entry=pair(segments[i]);accept(d,field(entry[0]),entry[1],b.range(),b,segments[i]);}
   if(inlineContext){for(int i=quote;i<segments.length;i++){var entry=pair(segments[i]);accept(d,field(entry[0]),entry[1],b.range(),b,segments[i]);}}
   else accept(d,"displayQuote",whole,b.range(),b,original);
   String path=d.participant==null?"/inference/inlineQuote":"/participations/"+d.participant.get("candidateParticipationKey")+"/displayQuote";
   d.conflicts.add(map("kind","C3","fieldPath",path,"reasonCode","INLINE_QUOTE","values",List.of("台詞全体: "+whole,"Inline項目: "+String.join(" ／ ",Arrays.copyOfRange(segments,quote,segments.length))),"interpretations",List.of(map("type","QUOTE","value",whole),map("type","INLINE","fields",alternatives)),"evidenceId",b.id()));unknown(d,original,b.range(),b);
   for(var trace:d.traces)if(Objects.equals(trace.get("startOffset"),b.range().startOffset())&&text(trace.get("fieldPath")).startsWith("/participations/")&&alternatives.stream().anyMatch(a->text(trace.get("fieldPath")).endsWith("/"+switch(text(a.get("field"))){case "pc"->"pcName";case "stateSan","stateHp","stateMp"->"endPcState/statusValues/"+text(a.get("field")).substring(5).toLowerCase(Locale.ROOT);default->text(a.get("field"));}))){trace.put("derivation","UNRESOLVED");trace.put("interpretationEvidenceId",b.id());}return;
  }
  for(String segment:segments){var entry=pair(segment);String key=field(entry[0]);boolean delimited=segment.indexOf(':')>=0||segment.indexOf('：')>=0;accept(d,key,delimited?entry[1]:segment,b.range(),b,segment);}
 }
 private void unknown(Draft d,String value,Range r,Block b) {
  if(value.isBlank())return;String item="u-"+UUID.randomUUID();list(d.data.get("unassignedValues")).add(map("itemKey",item,"rawValue",value,"assignment",map("type","UNASSIGNED","candidateParticipationKey",null)));d.traces.add(r.trace("/unassignedValues/"+item,b.logicalRecord(),b.id()+":"+r.startOffset(),"DIRECT",List.of()));
 }
 private void emit(Draft d,List<ImportParser.Parsed> out,String format) {
  if(!d.populated())return;finalizeRoles(d);resolveKnownSkp(d);ImportDrafts.normalizeSpotlight(d.data);
  for(var p:list(d.data.get("participations")))if(p.get("role")==null&&d.relations.stream().noneMatch(x->Objects.equals(x.get("candidateParticipationKey"),p.get("candidateParticipationKey"))))d.relations.add(map("candidateParticipationKey",p.get("candidateParticipationKey"),"state","UNDETERMINED","evidenceIds",List.of()));
  out.add(new ImportParser.Parsed(d.data,d.start==Integer.MAX_VALUE?1:d.start,Math.max(1,d.end),d.excerpt.toString(),d.traces,map("format",format,"recordStartOffset",d.recordStart,"recordEndOffset",d.recordEnd,"scopeEvidenceId",d.scopeEvidenceId,"relations",d.relations,"conflicts",d.conflicts,"boundary",d.explicitTable?"E3":"E0","sharedContextEvidenceIds",d.usedContext,"quotedEvidence",d.quotedEvidence)));
 }
 private LocalDate recordedDate(String value){
  String normalized=java.text.Normalizer.normalize(value.strip(),java.text.Normalizer.Form.NFKC);
  try{return LocalDate.parse(normalized);}catch(java.time.format.DateTimeParseException ignored){}
  var full=Pattern.compile("^([0-9]{4})[/年.\\-]([0-9]{1,2})[/月.\\-]([0-9]{1,2})日?$").matcher(normalized);
  if(!full.matches())throw new IllegalArgumentException("An explicit year is required");
  return LocalDate.of(Integer.parseInt(full.group(1)),Integer.parseInt(full.group(2)),Integer.parseInt(full.group(3)));
 }
 private String[] pair(String text){var p=text.split("[:：]",2);if(p.length!=2)return new String[]{text.strip(),""};String value=p[1];if(field(p[0]).equals("displayQuote")){if(value.startsWith(" "))value=value.substring(1);}else value=value.strip();return new String[]{p[0].strip(),value};}
 static String field(String label) {
  return switch(label.strip().toLowerCase(Locale.ROOT)) {
   case "シナリオ","シナリオ名","scenario","scenarioname"->"scenarioName";
   case "ゲームシステム","システム","system","gamesystemname"->"gameSystemName";
   case "卓名","table","tablename"->"tableName";
   case "日付","実施日","開催日","date","playedon"->"date";
   case "録画url","録画","recordingurl"->"recordingUrl";
   case "作者","作者名","シナリオ作者","authorname"->"authorName";
   case "シナリオurl","sourceurl","scenario url"->"sourceUrl";
   case "セリフ","台詞","飾り台詞","quote","displayquote"->"displayQuote";
   case "spotlight","spotlighttype"->"spotlightType";
   case "キャラクターシートurl","キャラシurl","character sheet url","charactersheeturl"->"characterSheetUrl";
   case "終了san","終了時san"->"stateSan";case "終了hp","終了時hp"->"stateHp";case "終了mp","終了時mp"->"stateMp";
   case "成長","growth"->"stateGrowth";case "結末","outcome"->"stateOutcome";case "後遺症","aftereffects"->"stateAftereffects";
   case "kp"->"KP";case "pl"->"PL";case "skp"->"SKP";
   case "氏名","人物","人物名","参加者","person","personname","pl名"->"person";
   case "pc","pc名","使用pc","pcname"->"pc";
   case "役割","role"->"role";case "ho","handout"->"handout";
   case "記録","状況","状態","context","status"->"context";
   case "進行","ゲーム進行","進行担当"->"progress";
   default->"unknown";
  };
 }
}
