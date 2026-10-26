package jp.takukairo;

import jp.takukairo.importsession.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.*;
import static jp.takukairo.common.Json.*;
import static org.junit.jupiter.api.Assertions.*;

class ImportReadinessRegressionTest {
 private final ImportParser parser=new ImportParser();

 @Test void sharedDatesKeepValuesAndSourceLocationsInAllFormats(){
  var cases=new LinkedHashMap<String,String>();
  cases.put("dates.md","シナリオ: A\n日付: 2026-10-01\n日付: 2026-10-02\n\n|PL|PC|\n|---|---|\n|田中|青い月|");
  cases.put("dates.html","<p>シナリオ: A</p><p>日付: 2026-10-01</p><p>日付: 2026-10-02</p><table><tr><th>PL</th><th>PC</th></tr><tr><td>田中</td><td>青い月</td></tr></table>");
  cases.put("dates.txt","シナリオ: A\n日付: 2026-10-01\n日付: 2026-10-02\nPL\tPC\n田中\t青い月");
  cases.put("dates.csv","シナリオ名,日付,日付,PL,PC\nA,2026-10-01,2026-10-02,田中,青い月");
  cases.put("paste","シナリオ: A\n日付: 2026-10-01\n日付: 2026-10-02\nPL: 田中\nPC: 青い月");
  cases.forEach((name,raw)->{
   var pieces=parser.parse(name.equals("paste")?null:name,raw);assertEquals(1,pieces.size(),name);var p=pieces.getFirst();
   assertEquals(List.of("2026-10-01","2026-10-02"),list(p.data().get("tableDates")).stream().map(d->text(d.get("playedOn"))).toList(),name);
   var dates=p.traces().stream().filter(t->"/table/tableDates".equals(t.get("fieldPath"))).toList();assertEquals(2,dates.size(),name);
   for(var trace:dates)assertEquals(trace.get("excerpt"),raw.substring(((Number)trace.get("startOffset")).intValue(),((Number)trace.get("endOffset")).intValue()));
  });
 }

 @Test void sharedDatesStopAtNextTableAndKeepRowDates(){
  String raw="<p>シナリオ: A</p><p>日付: 2026-10-01</p><table><tr><th>PL</th><th>日付</th></tr><tr><td>田中</td><td>2026-10-02</td></tr><tr><td>佐藤</td><td>2026-10-03</td></tr></table><table><tr><th>PL</th></tr><tr><td>別卓</td></tr></table>";
  var pieces=parser.parse("dates.html",raw);assertEquals(3,pieces.size());
  assertEquals(List.of("2026-10-01","2026-10-02"),list(pieces.get(0).data().get("tableDates")).stream().map(d->d.get("playedOn")).toList());
  assertEquals(List.of("2026-10-01","2026-10-03"),list(pieces.get(1).data().get("tableDates")).stream().map(d->d.get("playedOn")).toList());
  assertTrue(list(pieces.get(2).data().get("tableDates")).isEmpty());
  assertFalse(pieces.get(2).traces().stream().anyMatch(t->"/table/tableDates".equals(t.get("fieldPath"))));
 }

 @ParameterizedTest @ValueSource(strings={"プレイ済みではなかった","実施済みとは限らない","not actually played","never really completed","hasn't played","hasn’t played","実施済みではないわけではない","実施済みかもしれない"})
 void negativeOrUncertainContextDoesNotInferParticipation(String context){
  var csv=parser.parse("x.csv","シナリオ名,ゲームシステム,記録,氏名,PC,進行\nA,CoC,"+context+",田中,青い月,佐藤").getFirst();
  assertTrue(list(csv.data().get("participations")).stream().allMatch(p->p.get("role")==null),context);
  assertTrue(list(csv.data().get("unassignedValues")).stream().anyMatch(u->text(u.get("rawValue")).contains(context)),context);
  var md=parser.parse("x.md","# シナリオ: A\n## 記録: 実施済み\n### 記録: "+context+"\n氏名: 田中\nPC: 青い月\n### その他\n氏名: 鈴木\nPC: 白い雲");
  var people=md.stream().flatMap(p->list(p.data().get("participations")).stream()).toList();assertNull(people.getFirst().get("role"),context);assertEquals("PL",people.getLast().get("role"),context);
 }

 @ParameterizedTest @ValueSource(strings={"実施済み","プレイ済み参加者記録","通過済み","actually played","has already completed"})
 void affirmativeContextStillInfersPl(String context){
  var p=parser.parse("x.csv","シナリオ名,記録,氏名,PC\nA,"+context+",田中,青い月").getFirst();assertEquals("PL",list(p.data().get("participations")).getFirst().get("role"));
 }

 @Test void assembledSkpUsesMainKpInEitherOrderAndExplainsItsRole(){
  for(boolean mainFirst:List.of(true,false)){
   String raw="# シナリオ: A\n## 卓名: T\n### 参加者\n"+(mainFirst?"KP: 佐藤":"SKP: 田中")+"\n### その他\n"+(mainFirst?"SKP: 田中":"KP: 佐藤");
   var p=parser.parse("roles.md",raw).getFirst();var people=list(p.data().get("participations"));assertEquals(List.of("佐藤","田中"),people.stream().map(x->x.get("personName")).toList());assertTrue(people.stream().allMatch(x->"KP".equals(x.get("role"))));
   var relation=list(p.inference().get("relations")).stream().filter(r->"SKP".equals(r.get("originalRole"))).findFirst().orElseThrow();assertEquals(true,relation.get("hasMainKp"));assertEquals("PROVISIONAL",relation.get("state"));
   String key=text(people.getLast().get("candidateParticipationKey"));var trace=p.traces().stream().filter(t->("/participations/"+key+"/role").equals(t.get("fieldPath"))).findFirst().orElseThrow();assertEquals("INFERRED",trace.get("derivation"));assertTrue(((List<?>)trace.get("dependsOn")).size()>=2);
   assertEquals(raw.length(),p.inference().get("recordEndOffset"));
  }
 }

 @Test void skpCannotBorrowMainFromAnotherTableOrExcludedContext(){
  var separate=parser.parse("roles.md","# シナリオ: A\n## 卓名: 一卓\nKP: 佐藤\n## 卓名: 二卓\nSKP: 田中");assertEquals(2,separate.size());assertNull(list(separate.getLast().data().get("participations")).getFirst().get("role"));
  var excluded=parser.parse("roles.md","# シナリオ: A\n## 卓名: T\n### 参加者\nKP: 佐藤\n### 記録: 予定\nSKP: 田中").getFirst();assertNull(list(excluded.data().get("participations")).getLast().get("role"));
 }
}
