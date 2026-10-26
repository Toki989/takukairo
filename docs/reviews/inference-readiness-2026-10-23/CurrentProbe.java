import jp.takukairo.importsession.*;
import static jp.takukairo.common.Json.*;
import java.util.*;
import java.nio.file.*;
public class CurrentProbe {
 static void parsed(String id,String name,String raw){try{var ps=new ImportParser().parse(name,raw);DeepProbe.emit(id,map("input",raw,"parsed",ps));}catch(Exception e){DeepProbe.emit(id,map("exception",e.toString()));}}
 public static void main(String[] args)throws Exception{
  System.setOut(new java.io.PrintStream(Files.newOutputStream(Path.of(args[0])),true,java.nio.charset.StandardCharsets.UTF_8));
  parsed("date-before-md-table","x.md","シナリオ: A\n日付: 2026-10-01\n\n|PL|PC|\n|---|---|\n|田中|青い月|");
  parsed("date-before-html-table","x.html","<p>シナリオ: A</p><p>日付: 2026-10-01</p><table><tr><th>PL</th><th>PC</th></tr><tr><td>田中</td><td>青い月</td></tr></table>");
  parsed("date-before-txt-table","x.txt","シナリオ: A\n日付: 2026-10-01\nPL\tPC\n田中\t青い月");
  parsed("date-inline-control",null,"シナリオ: A\n日付: 2026-10-01\nPL: 田中\nPC: 青い月");
  for(String s:List.of("プレイ済みではなかった","実施済みではありませんでした","実施済みとは限らない","not actually played","not yet played","実施済み"))parsed("context-"+s,"x.csv","シナリオ名,記録,氏名,PC\nA,"+s+",田中,青い月");
  parsed("main-and-skp-subheadings","x.md","# シナリオ: A\n## 卓名: T\n### KP\nKP: 佐藤\n### SKP\nSKP: 田中");
  parsed("main-and-skp-harmless-subheadings","x.md","# シナリオ: A\n## 卓名: T\n### 参加者\nKP: 佐藤\n### その他\nSKP: 田中");
  parsed("explicit-scope-record-range","x.md","# 卓名: T\n## シナリオ: A\nPL: 田中\n## ゲームシステム: CoC\nPL: 佐藤");
  parsed("html-inline-code","x.html","<p>シナリオ: A</p><code>PL: 引用人物</code>");
  parsed("md-inline-markup","x.md","シナリオ: **A**\nPL: **田中**\nPC: [青い月](https://example.com)");
  // Service probes use a memory Store: no formal writes or transaction claim.
  var x=DeepProbe.setup(null,"シナリオ: A\n日付: 2026-10-01\n作者: 作者A\nPL: 田中\nPC: 青い月");var c=DeepProbe.candidates(x).getFirst();var original=object(c.candidateData);var first=object(write(original));var second=object(write(original));first.put("authorName",null);second.put("authorName",null);first.put("tableDates",List.of());second.put("tableDates",List.of());
  first.put("participations",List.of());var body=map("expectedSessionVersion",0,"expectedVersion",0,"parts",List.of(map("clientPartKey","a","candidateData",first),map("clientPartKey","b","candidateData",second)));
  var preview=obj(x.service().preview(x.session().id,"SPLIT",c.id,body));DeepProbe.emit("split-dropped-scalars",map("original",original,"preview",preview));body.put("previewRevision",preview.get("previewRevision"));x.service().apply(x.session().id,"SPLIT",c.id,body);DeepProbe.emit("split-applied",DeepProbe.candidates(x).stream().map(v->map("id",v.id,"data",object(v.candidateData),"inference",object(v.inferenceData))).toList());
  x=DeepProbe.setup(null,"シナリオ: A\n日付: 2026-10-01\nPL: 田中\n\nシナリオ: A\n日付: 2026-10-02\nPL: 佐藤");var cs=DeepProbe.candidates(x);var merged=object(cs.getFirst().candidateData);list(merged.get("participations")).addAll(list(object(cs.getLast().candidateData).get("participations")));body=map("expectedSessionVersion",0,"sources",cs.stream().map(v->map("candidateId",v.id,"expectedVersion",0)).toList(),"mergedCandidateData",merged);DeepProbe.emit("merge-dropped-date",x.service().preview(x.session().id,"MERGE",null,body));
  x=DeepProbe.setup(null,"シナリオ: A\nPL: 田中\nPC: 元PC\n新PC");c=DeepProbe.candidates(x).getFirst();var d=object(c.candidateData);var person=list(d.get("participations")).getFirst();var u=list(d.get("unassignedValues")).getFirst();u.put("assignment",map("type","PARTICIPATION_PC","candidateParticipationKey",person.get("candidateParticipationKey")));x.service().patch(x.session().id,c.id,map("expectedSessionVersion",0,"expectedVersion",0,"candidateData",d));
  first=object(write(d));second=object(write(d));first.put("participations",List.of());second.put("unassignedValues",List.of());body=map("expectedSessionVersion",0,"expectedVersion",0,"parts",List.of(map("clientPartKey","a","candidateData",first),map("clientPartKey","b","candidateData",second)));preview=obj(x.service().preview(x.session().id,"SPLIT",c.id,body));DeepProbe.emit("split-assigned-person-moved-preview",preview);body.put("previewRevision",preview.get("previewRevision"));x.service().apply(x.session().id,"SPLIT",c.id,body);DeepProbe.emit("split-assigned-person-moved-applied",DeepProbe.candidates(x).stream().map(v->map("id",v.id,"data",object(v.candidateData),"status",v.status)).toList());
 }
}
