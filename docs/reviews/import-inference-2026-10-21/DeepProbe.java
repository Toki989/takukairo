import jp.takukairo.importsession.*;
import jp.takukairo.common.*;
import jp.takukairo.model.*;
import jp.takukairo.security.CurrentUser;
import jp.takukairo.gamesystem.Profiles;
import static jp.takukairo.common.Json.*;
import java.util.*;
import java.nio.file.*;

/** Review counterexamples only. No application server, database, or formal writes. */
public class DeepProbe {
 static class MemoryStore extends Store {
  long next=1; Map<Class<?>,LinkedHashMap<Long,Object>> rows=new HashMap<>();
  public <T>T save(T row){try{var id=row.getClass().getField("id");if(id.get(row)==null)id.set(row,next++);rows.computeIfAbsent(row.getClass(),k->new LinkedHashMap<>()).put((Long)id.get(row),row);return row;}catch(Exception e){throw new RuntimeException(e);}}
  public <T>T find(Class<T> type,long id){var row=rows.getOrDefault(type,new LinkedHashMap<>()).get(id);if(row==null)throw ApiException.notFound();return type.cast(row);}
  public <T>T lock(Class<T> type,long id){return find(type,id);}
  public <T>List<T> query(Class<T> type,String where,Object...args){var out=new ArrayList<T>();for(var row:rows.getOrDefault(type,new LinkedHashMap<>()).values()){boolean keep=true;for(int i=0;i<args.length;i+=2){var match=java.util.regex.Pattern.compile("e\\.(\\w+)\\s*(=|in)\\s*:"+args[i]+"\\b").matcher(where);if(match.find()){try{Object actual=row.getClass().getField(match.group(1)).get(row);keep&=match.group(2).equals("in")?((Collection<?>)args[i+1]).contains(actual):Objects.equals(actual,args[i+1]);}catch(Exception e){throw new RuntimeException(e);}}}if(keep)out.add(type.cast(row));}return out;}
  public void flush(){} public void remove(Object row){try{rows.get(row.getClass()).remove(row.getClass().getField("id").get(row));}catch(Exception e){throw new RuntimeException(e);}}
 }
 record Setup(MemoryStore store,ImportService service,ImportSession session){}
 static Setup setup(String name,String raw){var store=new MemoryStore();var session=new ImportSession();session.userId=1L;session.status="ACTIVE";store.save(session);var source=new ImportSource();source.importSessionId=session.id;source.rawText=raw;source.originalFileName=name;source.sourceType=name==null?"PASTED_TEXT":"FILE";source.sourceOrder=1;store.save(source);var owner=new CurrentUser(store){public long id(){return 1L;}public <T>T owned(Class<T> type,long id){return store.find(type,id);}};var svc=new ImportService(store,owner,null,null,new ImportParser(),null,null,new Profiles());svc.analyze(session.id,map("expectedSessionVersion",0));return new Setup(store,svc,session);}
 static List<ImportCandidate> candidates(Setup x){return x.store.query(ImportCandidate.class,"where e.importSessionId=:id","id",x.session.id);}
 static Object invoke(ImportService service,String name,Class<?>[] types,Object...args)throws Exception{var method=ImportService.class.getDeclaredMethod(name,types);method.setAccessible(true);try{return method.invoke(service,args);}catch(java.lang.reflect.InvocationTargetException e){throw (Exception)e.getCause();}}
 static void emit(String id,Object result){System.out.println(write(map("id",id,"result",result)));}
 static void parsed(String id,String name,String input){try{var parsed=new ImportParser().parse(name,input);emit(id,map("input",input,"parsed",parsed));}catch(Exception e){emit(id,map("exception",e.toString()));}}
 static Map<String,Object> data(String scenario){return map("scenarioName",scenario,"gameSystemName",null,"tableName",null,"tableDates",new ArrayList<>(),"recordingUrl",null,"participations",new ArrayList<>(),"unassignedValues",new ArrayList<>());}
 public static void main(String[]args)throws Exception{
  System.setOut(new java.io.PrintStream(Files.newOutputStream(Path.of(args[0])),true,java.nio.charset.StandardCharsets.UTF_8));
  // Previous five findings, retained and exercised on current sources.
  parsed("R02-rowspan","x.html","<table><tr><th>シナリオ名</th><th>PL</th><th>PC</th></tr><tr><td rowspan='2'>A</td><td>田中</td><td>青い月</td></tr><tr><td>佐藤</td><td>赤い星</td></tr></table>");
  parsed("R03-negative","x.csv","シナリオ名,記録,氏名,PC\nA,プレイ済みではありません,田中,青い月");
  parsed("R04-paragraph-table","x.html","<p>シナリオ: A</p><p>ゲームシステム: CoC</p><p>記録: 実施済み</p><table><tr><th>氏名</th><th>PC</th></tr><tr><td>田中</td><td>青い月</td></tr></table>");
  parsed("R05-inline-roles",null,"シナリオ: A\nKP：佐藤、SKP：田中");
  var x=setup(null,"シナリオ: A\nPL: 田中\n\nシナリオ: A\n作者: 作者B\nシナリオURL: https://example.com/B\nPL: 佐藤");var cs=candidates(x);var merged=object(cs.getFirst().candidateData);list(merged.get("participations")).addAll(list(object(cs.getLast().candidateData).get("participations")));var mergeRequest=map("expectedSessionVersion",0,"sources",cs.stream().map(c->map("candidateId",c.id,"expectedVersion",0)).toList(),"mergedCandidateData",merged);emit("R01-merge-single-value",x.service.preview(x.session.id,"MERGE",null,mergeRequest));
  // Context scope, label syntax, and unsupported structure.
  parsed("N01-child-unknown-status","x.md","# シナリオ: A\n## 記録: 実施済み\n### 記録: 未確認\n氏名: 田中\nPC: 青い月");
  parsed("N02-punctuation-heading","x.md","# シナリオ: A\n## 卓名: T\nPL: 田中\nPC: 青い月\n## 卓名: T\nPL: 佐藤\nPC: 赤い星");
  parsed("N03-md-no-border","x.md","シナリオ名 | PL | PC\n--- | --- | ---\nA | 田中 | 青い月");
  parsed("N04-md-escaped-pipe","x.md","|シナリオ名|PL|Quote|\n|---|---|---|\n|A|田中|左\\|右|");
  parsed("N05-md-indented-table","x.md","  |シナリオ名|PL|PC|\n  |---|---|---|\n  |A|田中|青い月|");
  parsed("N06-html-th-single","x.html","<table><tr><th>シナリオ名</th><th>PL</th></tr><tr><th>A</th><td>田中</td></tr></table>");
  parsed("N07-html-nested-table","x.html","<table><tr><th>シナリオ名</th><th>PL</th></tr><tr><td>A</td><td><table><tr><th>PL</th><th>PC</th></tr><tr><td>田中</td><td>青い月</td></tr></table></td></tr></table>");
  parsed("N08-md-thematic-break","x.md","シナリオ: A\n\n***\n\nPL: 田中");
  parsed("N09-html-inline-code","x.html","<p>シナリオ: A</p><code>PL: 引用人物</code>");
  parsed("N10-md-inline-code","x.md","シナリオ: A\n`PL: 引用人物`");
  parsed("N11-label-skp-pc","x.csv","シナリオ名,KP,氏名,役割\nA,主KP,補助,SKP");
  // Branches where effective data and all stored references disagree.
  x=setup(null,"シナリオ: A\nPL: 正常\nPL: 除外");var c=candidates(x).getFirst();var d=object(c.candidateData);var parts=list(d.get("participations"));parts.getLast().put("saveDecision","OMIT");var changes=List.of(map("slot",map("type","SCENARIO"),"decision","CREATE_NEW","draftData",map("name","A")),map("slot",map("type","PERSON","candidateParticipationKey",parts.getFirst().get("candidateParticipationKey")),"decision","CREATE_NEW","draftData",map("displayName","正常")),map("slot",map("type","PC","candidateParticipationKey",parts.getLast().get("candidateParticipationKey")),"decision","CREATE_NEW","draftData",map("name","除外PC","characterSheetUrl","invalid-url")));emit("N12-omitted-invalid-reference",x.service.patch(x.session.id,c.id,map("expectedSessionVersion",0,"expectedVersion",0,"candidateData",d,"resolutionChanges",changes)));
  // Stable participation keys changed during split detach unresolved-role gates.
  x=setup(null,"シナリオ: A\nSKP: 田中");c=candidates(x).getFirst();d=object(c.candidateData);var p=list(d.get("participations")).getFirst();p.put("candidateParticipationKey","replacement-key");p.put("role","KP");var splitRequest=map("expectedSessionVersion",0,"expectedVersion",0,"parts",List.of(map("clientPartKey","a","candidateData",d),map("clientPartKey","b","candidateData",data("A"))));var splitPreview=obj(x.service.preview(x.session.id,"SPLIT",c.id,splitRequest));splitRequest.put("previewRevision",splitPreview.get("previewRevision"));x.service.apply(x.session.id,"SPLIT",c.id,splitRequest);var result=candidates(x).getFirst();boolean roleAccepted=true;try{invoke(x.service,"validateRoleChoices",new Class<?>[]{ImportCandidate.class,Map.class},result,object(result.candidateData));}catch(ApiException e){roleAccepted=false;}emit("N13-split-new-key",map("candidateData",object(result.candidateData),"inference",object(result.inferenceData),"roleAcceptedWithoutMeaningAcceptance",roleAccepted));
  // Omitted participation still contributes to E2 evidence.
  var b1=data("A");b1.put("tableDates",List.of(map("playedOn","2026-10-01")));b1.put("participations",List.of(map("candidateParticipationKey","one","personName","田中","pcName","青い月","role","PL","saveDecision","OMIT")));var b2=object(write(b1));list(b2.get("participations")).getFirst().put("saveDecision","SAVE");emit("N14-omitted-boundary",new ImportBoundary().evaluate(List.of(new ImportBoundary.Member(1,1,"one",b1,Map.of()),new ImportBoundary.Member(2,2,"two",b2,Map.of()))));
  // Long optional attributes are classified as registerable despite VARCHAR(255).
  x=setup(null,"シナリオ: A\n作者: "+"あ".repeat(256));c=candidates(x).getFirst();emit("N16-author-limit",x.service.patch(x.session.id,c.id,map("expectedSessionVersion",0,"expectedVersion",0,"resolutionChanges",List.of(map("slot",map("type","SCENARIO"),"decision","CREATE_NEW","draftData",map("name","A","authorName","あ".repeat(256)))))));
  // A malformed PC setting is also missed by resolution prevalidation.
  x=setup(null,"シナリオ: A\nPL: 田中\nPC: 青い月");c=candidates(x).getFirst();d=object(c.candidateData);p=list(d.get("participations")).getFirst();emit("N17-pc-setting-validation",x.service.patch(x.session.id,c.id,map("expectedSessionVersion",0,"expectedVersion",0,"resolutionChanges",List.of(map("slot",map("type","SCENARIO"),"decision","CREATE_NEW","draftData",map("name","A")),map("slot",map("type","PERSON","candidateParticipationKey",p.get("candidateParticipationKey")),"decision","CREATE_NEW","draftData",map("displayName","田中")),map("slot",map("type","PC","candidateParticipationKey",p.get("candidateParticipationKey")),"decision","CREATE_NEW","draftData",map("name","青い月","systemSettings",List.of(map("gameSystemName","CoC","canonicalSystemKey",null,"profileKey",null,"profileValues",map(),"characterSheetUrl","invalid-url"))))))));
  // Ordinary candidate PATCH can replace a key and evade the same SKP gate.
  x=setup(null,"シナリオ: A\nSKP: 田中");c=candidates(x).getFirst();d=object(c.candidateData);p=list(d.get("participations")).getFirst();p.put("candidateParticipationKey","new-key");p.put("role","KP");emit("N18-patch-new-key",x.service.patch(x.session.id,c.id,map("expectedSessionVersion",0,"expectedVersion",0,"candidateData",d,"confirmationStatus","CHECKED","resolutionChanges",List.of(map("slot",map("type","SCENARIO"),"decision","CREATE_NEW","draftData",map("name","A")),map("slot",map("type","PERSON","candidateParticipationKey","new-key"),"decision","CREATE_NEW","draftData",map("displayName","田中"))))));
  // Local evidence IDs are repeated in different Sources with distinct values.
  var parser=new ImportParser();var pa=parser.parse(null,"シナリオ: A\nPL: 田中\nPC: 青い月").getFirst();var pb=parser.parse(null,"シナリオ: B\nPL: 佐藤\nPC: 赤い星").getFirst();var ea=pa.traces().stream().map(t->t.get("evidenceId")).toList();var eb=pb.traces().stream().map(t->t.get("evidenceId")).toList();emit("N19-evidence-id-collision",map("sourceA",pa,"sourceB",pb,"duplicateIds",ea.stream().filter(eb::contains).toList()));
  parsed("N20-neutral-participation-context","x.csv","シナリオ名,記録,氏名,PC\nA,参加者募集記録,田中,青い月");
  parsed("N21-uncertain-context","x.csv","シナリオ名,記録,氏名,PC\nA,実施済みか未確認,田中,青い月");
  parsed("N22-atx-followed-by-rule","x.md","# シナリオ: A\n---\nPL: 田中");
  parsed("N23-table-caption-field","x.html","<table><caption>シナリオ: A</caption><tr><th>PL</th><th>PC</th></tr><tr><td>田中</td><td>青い月</td></tr></table>");
  parsed("N24-html-colspan","x.html","<table><tr><th>シナリオ名</th><th>PL</th><th>PC</th></tr><tr><td colspan='2'>A と参加者未定</td><td>青い月</td></tr></table>");
  // Intermediate structure operations require a fully resolved raw Role, even for OMIT.
  for(boolean omit:List.of(false,true)){
   x=setup(null,"シナリオ: A\nSKP: 田中");c=candidates(x).getFirst();d=object(c.candidateData);if(omit)list(d.get("participations")).getFirst().put("saveDecision","OMIT");
   try{var preview=x.service.preview(x.session.id,"SPLIT",c.id,map("expectedSessionVersion",0,"expectedVersion",0,"parts",List.of(map("clientPartKey","a","candidateData",d),map("clientPartKey","b","candidateData",data("A")))));emit("N25-split-unresolved-"+omit,preview);}catch(ApiException e){emit("N25-split-unresolved-"+omit,map("httpStatus",e.status,"errorCode",e.code,"fields",e.fields));}
  }
  // Assignment overwrites a value but user-edited source path does not follow it.
  x=setup(null,"シナリオ: A\nPL: 田中\nPC: 元PC\n新PC");c=candidates(x).getFirst();d=object(c.candidateData);p=list(d.get("participations")).getFirst();var u=list(d.get("unassignedValues")).getFirst();u.put("assignment",map("type","PARTICIPATION_PC","candidateParticipationKey",p.get("candidateParticipationKey")));var assignedPatch=x.service.patch(x.session.id,c.id,map("expectedSessionVersion",0,"expectedVersion",0,"candidateData",d,"resolutionChanges",List.of(map("slot",map("type","SCENARIO"),"decision","CREATE_NEW","draftData",map("name","A")),map("slot",map("type","PERSON","candidateParticipationKey",p.get("candidateParticipationKey")),"decision","CREATE_NEW","draftData",map("displayName","田中")),map("slot",map("type","PC","candidateParticipationKey",p.get("candidateParticipationKey")),"decision","CREATE_NEW","draftData",map("name","元PC")))));var effective=ImportDrafts.effective(object(c.candidateData));invoke(x.service,"applyAssignments",new Class<?>[]{Map.class},effective);emit("N15-assignment-provenance",map("effective",effective,"inference",object(c.inferenceData),"patch",assignedPatch));
 }
}
