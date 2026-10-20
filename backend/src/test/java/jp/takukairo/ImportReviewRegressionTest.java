package jp.takukairo;

import jp.takukairo.importsession.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static jp.takukairo.common.Json.*;

/** Counterexamples from the 2026-10-15 review, independent of user acceptance fixtures. */
class ImportReviewRegressionTest {
 final ImportParser parser=new ImportParser();
 Map<String,Object> part(ImportParser.Parsed p){return list(p.data().get("participations")).getFirst();}
 @Test void r2MixedHtmlOwnTextAndUnsupportedWrappersRemainReviewable(){
  for(String raw:List.of("<div>PL: 田中<p>シナリオ: A</p></div>","<section>PL: 田中<article><p>シナリオ: A</p>未所属の記録</article></section>")){
   var parsed=parser.parse("a.html",raw);assertTrue(parsed.stream().anyMatch(p->list(p.data().get("participations")).stream().anyMatch(x->"田中".equals(x.get("personName")))));
   assertTrue(parsed.stream().anyMatch(p->"A".equals(p.data().get("scenarioName"))));
   if(raw.contains("未所属"))assertTrue(parsed.stream().flatMap(p->list(p.data().get("unassignedValues")).stream()).anyMatch(x->text(x.get("rawValue")).contains("未所属")));
  }
  assertEquals("A",parser.parse("a.md","# シナリオ: A").getFirst().data().get("scenarioName"));
 }
 @Test void r3QuotedHtmlTablesAndPreNeverCreateActualRoles(){
  String table="<table><tr><th>シナリオ名</th><th>PL</th><th>PC</th></tr><tr><td>A</td><td>田中</td><td>青い月</td></tr></table>";
  for(String raw:List.of("<blockquote>"+table+"</blockquote>","<pre>シナリオ: A<br>PL: 田中<br>PC: 青い月</pre>","<blockquote><h2>卓名: 引用卓</h2>"+table+"</blockquote>")){
   var p=parser.parse("a.html",raw);assertFalse(p.isEmpty());assertTrue(p.stream().flatMap(x->list(x.data().get("participations")).stream()).allMatch(x->x.get("role")==null));
   assertTrue(p.stream().anyMatch(x->!list(x.data().get("unassignedValues")).isEmpty()));
  }
 }
 @Test void r4CompetingRowContextIsOrderIndependentAndRetainsBothEvidence(){
  for(String contexts:List.of("予定,実施済み","実施済み,予定")){
   var p=parser.parse("a.csv","シナリオ名,記録,記録,氏名,PC\nA,"+contexts+",田中,青い月").getFirst();assertNull(part(p).get("role"));
   var conflict=list(p.inference().get("conflicts")).stream().filter(x->"/inference/context".equals(x.get("fieldPath"))).findFirst().orElseThrow();assertEquals("C3",conflict.get("kind"));assertEquals(Set.of("予定","実施済み"),new HashSet<>((List<?>)conflict.get("values")));
   assertEquals(2,p.traces().stream().filter(x->"/inference/context".equals(x.get("fieldPath"))).count());
  }
 }
 @Test void r4LateParagraphContextStopsAtParticipantBoundary(){
  var p=parser.parse("a.txt","シナリオ: A\n氏名: 田中\nPC: 青い月\n記録: 実施済み\n氏名: 佐藤\nPC: 赤い星\n記録: 予定").getFirst();var parts=list(p.data().get("participations"));assertEquals("PL",parts.getFirst().get("role"));assertNull(parts.getLast().get("role"));
  String key=text(parts.getFirst().get("candidateParticipationKey"));var role=p.traces().stream().filter(x->("/participations/"+key+"/role").equals(x.get("fieldPath"))).findFirst().orElseThrow();assertFalse(((List<?>)role.get("dependsOn")).stream().anyMatch(x->text(x).startsWith("b-7:")));
 }
 @Test void r5BrAndTdHeadersMatchAllOtherFormats(){
  var cases=Map.of("a.csv","シナリオ名,PL,PC\nA,田中,青い月","a.md","|シナリオ名|PL|PC|\n|---|---|---|\n|A|田中|青い月|","a.html","<p>シナリオ: A<br>PL: 田中<br>PC: 青い月</p>","table.html","<table><tr><td>シナリオ名</td><td>PL</td><td>PC</td></tr><tr><td>A</td><td>田中</td><td>青い月</td></tr></table>","a.txt","シナリオ: A\nPL: 田中\nPC: 青い月","paste","シナリオ: A\nPL: 田中\nPC: 青い月");
  for(var c:cases.entrySet()){var p=parser.parse(c.getKey().equals("paste")?null:c.getKey(),c.getValue());assertEquals(1,p.size(),c.getKey());assertEquals("A",p.getFirst().data().get("scenarioName"),c.getKey());assertEquals("田中",part(p.getFirst()).get("personName"));assertEquals("青い月",part(p.getFirst()).get("pcName"));assertEquals("PL",part(p.getFirst()).get("role"));}
 }
 @Test void r6InlinePcHasTraceToItsOwnFieldAndEvidence(){
  String raw="シナリオ: A\n記録: 実施済み\n氏名: 田中（PC: 青い月）";var p=parser.parse("a.txt",raw).getFirst();String key=text(part(p).get("candidateParticipationKey"));
  for(String field:List.of("personName","pcName","role")){var t=p.traces().stream().filter(x->("/participations/"+key+"/"+field).equals(x.get("fieldPath"))).findFirst().orElseThrow();assertEquals(raw.substring(((Number)t.get("startOffset")).intValue(),((Number)t.get("endOffset")).intValue()),t.get("excerpt"));assertTrue(text(t.get("excerpt")).contains(field.equals("personName")?"田中":"青い月"));}
 }
 @Test void r7QuoteWhitespaceAndUnicodeStayExactInAllFormats(){
  for(String quote:List.of(" まだ帰らない ","e\u0301".repeat(24),"👨‍👩‍👧‍👦".repeat(25),"　引用　")){
   var cases=Map.of("a.csv","シナリオ名,PL,Quote\nA,田中,\""+quote+"\"","a.md","|シナリオ名|PL|Quote|\n|---|---|---|\n|A|田中|"+quote+"|","a.html","<table><tr><td>シナリオ名</td><td>PL</td><td>Quote</td></tr><tr><td>A</td><td>田中</td><td>"+quote+"</td></tr></table>","a.txt","シナリオ: A\nPL: 田中\nQuote: "+quote,"paste","シナリオ: A\nPL: 田中\nQuote: "+quote);
   for(var c:cases.entrySet())assertEquals(quote,part(parser.parse(c.getKey().equals("paste")?null:c.getKey(),c.getValue()).getFirst()).get("displayQuote"),c.getKey());
  }
 }
 @Test void r7QuoteLineBreaksRemainExactInCsvAndHtml(){
  for(var c:Map.of("a.csv","シナリオ名,PL,Quote\nA,田中,\" 一行\n二行 \"","a.html","<table><tr><th>シナリオ名</th><th>PL</th><th>Quote</th></tr><tr><td>A</td><td>田中</td><td> 一行<br>二行 </td></tr></table>").entrySet())assertEquals(" 一行\n二行 ",part(parser.parse(c.getKey(),c.getValue()).getFirst()).get("displayQuote"));
 }
 @Test void r3And4AdjacentTableContextStopsAtFollowingParagraph(){
  for(String status:List.of("予定","実施済み")){
   String html="<p>記録: "+status+"</p><table><tr><th>氏名</th><th>PC</th></tr><tr><td>田中</td><td>青い月</td></tr><tr><td>佐藤</td><td>赤い星</td></tr></table><p>氏名: 別記録</p><p>PC: 別PC</p>";
   var parsed=parser.parse("context.html",html);assertEquals(3,parsed.size());var people=parsed.stream().flatMap(p->list(p.data().get("participations")).stream()).toList();assertEquals(3,people.size());assertEquals(status.equals("予定")?null:"PL",people.get(0).get("role"));assertEquals(status.equals("予定")?null:"PL",people.get(1).get("role"));assertNull(people.get(2).get("role"));
  }
  var progress=parser.parse("progress.txt","ゲームシステム: CoC\n氏名: 田中\nPC: 青い月\n記録: 実施済み\n進行: 別人物");assertTrue(progress.stream().flatMap(p->list(p.data().get("participations")).stream()).noneMatch(p->"KP".equals(p.get("role"))));
 }
 @Test void r4MultipleConflictingRowsInExplicitScopeKeepIndependentEvidence(){
  var p=parser.parse("contexts.md","# シナリオ: A\n## 卓名: T\n|記録|記録|PL|PC|\n|---|---|---|---|\n|予定|実施済み|田中|青い月|\n|実施済み|予定|佐藤|赤い星|").getFirst();assertEquals(2,list(p.inference().get("conflicts")).stream().filter(x->"/inference/context".equals(x.get("fieldPath"))).count());var parts=list(p.data().get("participations"));assertTrue(parts.stream().allMatch(x->x.get("role")==null));String first=text(parts.getFirst().get("candidateParticipationKey"));var relation=list(p.inference().get("relations")).stream().filter(x->first.equals(x.get("candidateParticipationKey"))).findFirst().orElseThrow();assertTrue(((List<?>)relation.get("evidenceIds")).stream().noneMatch(x->text(x).startsWith("b-6:")));
 }
}
