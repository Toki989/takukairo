package jp.takukairo;

import jp.takukairo.importsession.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static jp.takukairo.common.Json.*;

/** Internal counterexamples, not the user's formal T01–T24 acceptance fixtures. */
class ImportCurrentDefectsTest {
 final ImportParser parser=new ImportParser();
 Map<String,Object> part(ImportParser.Parsed p){return list(p.data().get("participations")).getFirst();}
 @Test void meaningfulUnusedHeadingsAndSingleRowsAreAccountedFor(){
  for(String raw:List.of("# 2026-10-14 卓Aを田中とプレイ","# 2026-10-14 卓Aを田中とプレイ\n# シナリオ: B\nPL: 佐藤","|2026-10-14 卓Aを田中とプレイ|","|活動内容|\n|---|")){
   var parsed=parser.parse("a.md",raw);assertTrue(parsed.stream().flatMap(p->p.traces().stream()).anyMatch(t->text(t.get("excerpt")).contains(raw.split("\n")[0].replace("# ",""))),raw);
  }
 }
 @Test void emptyFieldsNeverBecomeNamesOrRoleEvidence(){
  for(String name:List.of("a.txt","a.md","paste")){
   var p=parser.parse(name.equals("paste")?null:name,"シナリオ:\nPL: 田中").getFirst();assertEquals("",p.data().get("scenarioName"));
   p=parser.parse(name.equals("paste")?null:name,"シナリオ: A\nPL:\nPC:").getFirst();assertTrue(list(p.data().get("participations")).isEmpty());
   p=parser.parse(name.equals("paste")?null:name,"シナリオ: A\n記録: 実施済み\n氏名: 田中\nPC:").getFirst();assertNull(part(p).get("pcName"));assertNull(part(p).get("role"));
  }
 }
 @Test void decorationAndBenignHeadingsDoNotAddUnassignedBlockers(){
  assertTrue(parser.parse("a.md","# 概要\n\n---\n# 目次\n***").isEmpty());var p=parser.parse("a.txt","シナリオ: A\n---\n***\nPL: 田中").getFirst();assertTrue(list(p.data().get("unassignedValues")).isEmpty());
  var parsed=parser.parse("a.md","# 2026-10-14 卓Aを田中とプレイ\n所属不明の補足");assertTrue(parsed.stream().anyMatch(x->x.excerpt().contains("2026-10-14")));
 }
 @Test void negativeCandidateAndQuotedContextNeverSupplyCompletedEvidence(){
  for(String context:List.of("参加者候補記録","not completed","not yet played","uncompleted","未プレイ済み","未実施","実施済みではない","\"completed\"","「実施済み」")){
   var p=parser.parse("a.csv","シナリオ名,記録,氏名,PC\nA,"+(context.contains("\"")?"\"\"\"completed\"\"\"":context)+",田中,青い月").getFirst();assertNull(part(p).get("role"),context);assertFalse(list(p.data().get("unassignedValues")).isEmpty(),context);
  }
  for(String context:List.of("実施済み参加者記録","completed","played"))assertEquals("PL",part(parser.parse("a.csv","記録,氏名,PC\n"+context+",田中,青い月").getFirst()).get("role"));
 }
 @Test void uncertainQuoteKeepsBothInterpretationsAndRequiresReview(){
  for(String tail:List.of("私の／PC: 青い月","私は／HO: なんだろう")){
   var p=parser.parse("a.txt","シナリオ: A\nPL: 田中\nQuote: "+tail).getFirst();assertEquals(tail,part(p).get("displayQuote"));assertNull(part(p).get("pcName"));assertNull(part(p).get("handout"));assertFalse(list(p.data().get("unassignedValues")).isEmpty());assertTrue(list(p.inference().get("conflicts")).stream().anyMatch(c->"INLINE_QUOTE".equals(c.get("reasonCode"))));
  }
 }
 @Test void nonemptyQuoteWhitespaceIsNotChangedByTheEmptyFieldFix(){
  String q="  ";var cases=Map.of("a.csv","シナリオ名,PL,Quote\nA,田中,\""+q+"\"","a.md","|シナリオ名|PL|Quote|\n|---|---|---|\n|A|田中|"+q+"|","a.html","<table><tr><th>シナリオ名</th><th>PL</th><th>Quote</th></tr><tr><td>A</td><td>田中</td><td>"+q+"</td></tr></table>","a.txt","シナリオ: A\nPL: 田中\nQuote: "+q,"paste","シナリオ: A\nPL: 田中\nQuote: "+q);for(var entry:cases.entrySet())assertEquals(q,part(parser.parse(entry.getKey().equals("paste")?null:entry.getKey(),entry.getValue()).getFirst()).get("displayQuote"),entry.getKey());
 }
 @Test void negativeHeadingEvidenceStopsAtSiblingAndPositiveC05RemainsActive(){
  var parsed=parser.parse("a.md","# シナリオ: A\n## 記録: not completed\n### 卓名: T1\nPL: 田中\nPC: 青い月\n## 記録: completed\n### 卓名: T2\n氏名: 佐藤\nPC: 赤い星");assertEquals(2,parsed.size());assertNull(part(parsed.getFirst()).get("role"));var relation=list(parsed.getFirst().inference().get("relations")).getFirst();assertFalse(((List<?>)relation.get("evidenceIds")).isEmpty());assertEquals("PL",part(parsed.getLast()).get("role"));assertTrue(parsed.getFirst().traces().stream().anyMatch(t->"/inference/context".equals(t.get("fieldPath"))&&text(t.get("excerpt")).contains("not completed")));
 }
 @Test void explicitInlineExampleStillExtractsAttributesButRetainsQuoteAlternative(){
  var p=parser.parse("a.txt","シナリオ: A\nPL：田中／PC：青い月／HO：HO1／セリフ：まだ帰らない／終了SAN：53").getFirst();var person=part(p);assertEquals("青い月",person.get("pcName"));assertEquals("HO1",person.get("handout"));assertEquals("まだ帰らない",person.get("displayQuote"));assertEquals(53,((Number)obj(obj(person.get("endPcState")).get("statusValues")).get("san")).intValue());assertFalse(list(p.data().get("unassignedValues")).isEmpty());
 }
 @Test void repeatedTableAfterActivityRetainsSeparateMembershipHypotheses(){
  var parsed=parser.parse("a.txt","シナリオ: A\n卓名: T1\nPL: 田中\n卓名: T2\nPL: 佐藤");assertEquals(2,parsed.size());assertEquals(List.of("T1","T2"),parsed.stream().map(p->p.data().get("tableName")).toList());assertEquals(List.of("田中","佐藤"),parsed.stream().map(p->part(p).get("personName")).toList());assertTrue(parsed.stream().allMatch(p->list(p.data().get("participations")).size()==1&&"A".equals(p.data().get("scenarioName"))&&p.inference().get("scopeEvidenceId")==null));
  var beforeActivity=parser.parse("a.txt","シナリオ: A\n卓名: T1\n卓名: T2\nPL: 田中");assertEquals(1,beforeActivity.size());assertEquals("C3",list(beforeActivity.getFirst().inference().get("conflicts")).getFirst().get("kind"));
  var explicit=parser.parse("a.md","# シナリオ: A\n## 卓名: T1\nPL: 田中\n卓名: T2\nPL: 佐藤");assertEquals(1,explicit.size());assertEquals("E3",explicit.getFirst().inference().get("boundary"));assertFalse(list(explicit.getFirst().inference().get("conflicts")).isEmpty());
 }
 @Test void codeFencesRespectIndentCharacterLengthAndClosingSyntax(){
  for(String code:List.of("  ```\nPL: 引用人物\n  ```","   ~~~~txt\nPL: 引用人物\n~~~\nPL: まだ引用\n~~~~","````md\nPL: 引用人物\n```\nPL: まだ引用\n````","~~~\nPL: 引用人物\n```\nPL: まだ引用\n~~~","```\nPL: 引用人物\n``` suffix\nPL: まだ引用\n```"," ```\nPL: 引用人物")){
   String raw="シナリオ: A\n"+code;var parsed=parser.parse("a.md",raw);assertTrue(parsed.stream().flatMap(p->list(p.data().get("participations")).stream()).noneMatch(p->text(p.get("personName")).contains("引用")),code);assertTrue(parsed.stream().flatMap(p->p.traces().stream()).anyMatch(t->text(t.get("excerpt")).contains("PL: 引用人物")),code);
  }
  var p=parser.parse("a.md","シナリオ: A\n  ```\nPL: 引用人物\n  ```\nPL: 実参加").getFirst();assertEquals("実参加",part(p).get("personName"));assertEquals(1,list(p.data().get("participations")).size());
 }
}
