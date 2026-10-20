package jp.takukairo;
import jp.takukairo.importsession.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static jp.takukairo.common.Json.*;
class ImportInferenceTest {
 final ImportParser parser=new ImportParser();
 @Test void allFiveFormatsHaveSameMeaningAndFieldTrace() {
  var cases=Map.of("a.csv","シナリオ名,ゲームシステム,卓名,日付,PL,PC,HO\nA,CoC,卓1,2026-10-01,五色 探/α,PC + β,HO1",
   "a.md","|シナリオ名|ゲームシステム|卓名|日付|PL|PC|HO|\n|---|---|---|---|---|---|---|\n|A|CoC|卓1|2026-10-01|五色 探/α|PC + β|HO1|",
   "a.html","<table><tr><th>シナリオ名</th><th>ゲームシステム</th><th>卓名</th><th>日付</th><th>PL</th><th>PC</th><th>HO</th></tr><tr><td>A</td><td>CoC</td><td>卓1</td><td>2026-10-01</td><td>五色 探/α</td><td>PC + β</td><td>HO1</td></tr></table>",
   "a.txt","シナリオ名: A\nゲームシステム: CoC\n卓名: 卓1\n日付: 2026-10-01\nPL: 五色 探/α\nPC: PC + β\nHO: HO1",
   "paste","シナリオ名: A\nゲームシステム: CoC\n卓名: 卓1\n日付: 2026-10-01\nPL: 五色 探/α\nPC: PC + β\nHO: HO1");
  for(var c:cases.entrySet()){var p=parser.parse(c.getKey().equals("paste")?null:c.getKey(),c.getValue()).getFirst();assertEquals("A",p.data().get("scenarioName"),c.getKey());assertEquals("卓1",p.data().get("tableName"));var part=list(p.data().get("participations")).getFirst();assertEquals("五色 探/α",part.get("personName"));assertEquals("PC + β",part.get("pcName"));assertEquals("PL",part.get("role"));assertEquals("HO1",part.get("handout"));assertTrue(list(p.data().get("unassignedValues")).isEmpty());assertTrue(p.traces().stream().anyMatch(t->String.valueOf(t.get("fieldPath")).endsWith("/pcName")));for(var trace:p.traces()){int a=((Number)trace.get("startOffset")).intValue(),z=((Number)trace.get("endOffset")).intValue();assertEquals(c.getValue().substring(a,z),trace.get("excerpt"));}}
 }
 @Test void csvQuotedCellsKeepNewlineCommaAndPhysicalRows() {
  String raw="シナリオ名,PL,PC\r\nA,\"五色, 探\r\n/α\",\"PC/ +β\"\r\nB,別人,別PC";var p=parser.parse("a.csv",raw);assertEquals(2,p.size());assertEquals("五色, 探\r\n/α",list(p.getFirst().data().get("participations")).getFirst().get("personName"));assertEquals(2,p.getFirst().lineStart());assertEquals(3,p.getFirst().lineEnd());assertEquals(4,p.getLast().lineStart());assertEquals(3L,p.getLast().traces().getFirst().get("logicalRecord"));assertTrue(p.getFirst().excerpt().contains("\"五色,"));
 }
 @Test void missingScenarioAndDuplicateHeadersNeverDiscardOrOverwrite() {
  assertEquals(1,parser.parse("x.txt","卓名: T\n日付: 10/13\nPL: 人物").size());var p=parser.parse("a.csv","シナリオ名,シナリオ名,PL\nA,B,人").getFirst();assertEquals("A",p.data().get("scenarioName"));assertFalse(list(p.inference().get("conflicts")).isEmpty());assertEquals("シナリオ名: B",list(p.data().get("unassignedValues")).getFirst().get("rawValue"));assertTrue(list(parser.parse("x.txt","日付: 10/13").getFirst().data().get("tableDates")).isEmpty());
 }
 @Test void completedPersonPcGetsPlButUnscopedRosterAndSkpDoNot() {
  var p=parser.parse("a.csv","記録,氏名,PC\n実施済み参加者記録,人物,使用PC").getFirst();assertEquals("PL",list(p.data().get("participations")).getFirst().get("role"));assertEquals("PROVISIONAL",list(p.inference().get("relations")).getFirst().get("state"));
  p=parser.parse("a.csv","氏名,PC\n人物,使用PC").getFirst();assertNull(list(p.data().get("participations")).getFirst().get("role"));p=parser.parse("a.txt","記録: 実施済み\nSKP: 補助\nPC: 人物").getFirst();assertNull(list(p.data().get("participations")).getFirst().get("role"));
 }
 @Test void plannedAuthorsAndNpcDoNotCreateActualRoles() {
  for(String context:List.of("予定","主催・日程調整","作者・投稿者・引用","NPC担当")){var p=parser.parse("a.csv","記録,氏名,PC\n"+context+",人物,PC").getFirst();assertNull(list(p.data().get("participations")).getFirst().get("role"));assertFalse(list(p.data().get("unassignedValues")).isEmpty());}
 }
 @Test void headingContextIsScopedAndParticipantsDoNotLeak() {
  var p=parser.parse("a.md","# シナリオ: A\n## 実施済み参加者記録\n### 卓名: T1\n氏名: 人物1\nPC: PC1\n### 卓名: T2\n氏名: 人物2\nPC: PC2\n# シナリオ: B\n氏名: 人物3\nPC: PC3");assertEquals(3,p.size());assertEquals("A",p.get(0).data().get("scenarioName"));assertEquals("T2",p.get(1).data().get("tableName"));assertEquals("PL",list(p.get(1).data().get("participations")).getFirst().get("role"));assertNull(list(p.get(2).data().get("participations")).getFirst().get("role"));assertEquals(1,list(p.get(1).data().get("participations")).size());
 }
 @Test void sameNamesAndDatesDoNotTransitivelyJoinRecords() {
  var p=parser.parse("a.csv","シナリオ名,卓名,日付,PL\nA,T,2026-01-01,人\nA,T,2026-01-02,人\nA,T,2026-01-01,人");assertEquals(3,p.size());assertTrue(p.stream().allMatch(x->"E0".equals(x.inference().get("boundary"))));
 }
 @Test void htmlSourceLinesAddressRawTagsInsteadOfRenderedText() {
  String raw="<div>\n<p>シナリオ: A</p><p>PL: 人物</p>\n</div>";var p=parser.parse("a.html",raw).getFirst();assertEquals(2,p.traces().getFirst().get("lineStart"));assertEquals("<p>シナリオ: A</p>",p.traces().getFirst().get("excerpt"));
 }
 @Test void explicitKpPlForSamePersonRemainTwoParticipationCandidates() {
  var p=parser.parse("a.txt","シナリオ: A\nKP: 人物\nPL: 人物").getFirst();assertEquals(2,list(p.data().get("participations")).size());assertEquals(List.of("KP","PL"),list(p.data().get("participations")).stream().map(x->x.get("role")).toList());
 }

 @Test void localPlannedContextAndLateContextAreReevaluatedBeforeRoleInference() {
  var p=parser.parse("a.md","# シナリオ: A\n## 実施済み参加者記録\n### 卓名: T1\n氏名: 人物1\nPC: PC1\n記録: 予定\n### 卓名: T2\n氏名: 人物2\nPC: PC2");assertNull(list(p.getFirst().data().get("participations")).getFirst().get("role"));assertEquals("PL",list(p.getLast().data().get("participations")).getFirst().get("role"));p=parser.parse("a.txt","氏名: 人物\nPC: PC\n記録: 実施済み参加者記録");assertEquals("PL",list(p.getFirst().data().get("participations")).getFirst().get("role"));
 }
 @Test void cocActualProgressCanInferKpButUnscopedProgressCannot() {
  var p=parser.parse("a.txt","ゲームシステム: CoC\n記録: 実施済み\n進行: 人物");assertEquals("KP",list(p.getFirst().data().get("participations")).getFirst().get("role"));p=parser.parse("a.txt","進行: 人物");assertTrue(list(p.getFirst().data().get("participations")).isEmpty());assertFalse(list(p.getFirst().data().get("unassignedValues")).isEmpty());
 }

 @Test void quotedRecordsDoNotCreateActualParticipationAndHeaderOnlyInputIsRetained() {
  for(var c:Map.of("a.md","> PL: 引用人物","a.html","<blockquote><p>PL: 引用人物</p></blockquote>").entrySet()){var p=parser.parse(c.getKey(),c.getValue()).getFirst();assertTrue(list(p.data().get("participations")).isEmpty());assertFalse(list(p.data().get("unassignedValues")).isEmpty());}
  var p=parser.parse("a.csv","氏名,PC").getFirst();assertFalse(list(p.data().get("unassignedValues")).isEmpty());
 }
 @Test void columnLabelsHaveOriginalEvidenceAndMarkdownSetextHierarchyIsRetained() {
  var p=parser.parse("a.csv","シナリオ名,PL\nA,人物").getFirst();assertEquals("シナリオ名",obj(p.traces().getFirst().get("labelLocation")).get("excerpt"));
  p=parser.parse("a.md","シナリオ: A\n===\n卓名: T\n---\nPL: 人物").getFirst();assertEquals("A",p.data().get("scenarioName"));assertEquals("T",p.data().get("tableName"));assertEquals("E3",p.inference().get("boundary"));
 }

 @Test void explicitRoleInPlannedContextIsObservedButNotActualParticipationRole() {
  for(String context:List.of("予定","主催・日程調整","作者・投稿者・引用","NPC担当")){var p=parser.parse("a.csv","シナリオ名,記録,PL,PC\nA,"+context+",人物,PC").getFirst();assertNull(list(p.data().get("participations")).getFirst().get("role"));assertTrue(list(p.inference().get("relations")).stream().anyMatch(r->"PL".equals(r.get("originalRole"))));}
 }
 @Test void rowContextExceptionsNeverInvalidateOtherParticipantsInExplicitTableScope() {
  var p=parser.parse("a.md","# シナリオ: A\n## 卓名: T\n|記録|氏名|PC|\n|---|---|---|\n|実施済み参加者記録|実参加|実PC|\n|予定|予定参加|予定PC|\n|実施済み参加者記録|別参加|別PC|").getFirst();var parts=list(p.data().get("participations"));assertEquals(3,parts.size());assertEquals("PL",parts.get(0).get("role"));assertNull(parts.get(1).get("role"));assertEquals("PL",parts.get(2).get("role"));String last=text(parts.get(2).get("candidateParticipationKey"));var trace=p.traces().stream().filter(x->("/participations/"+last+"/role").equals(x.get("fieldPath"))).findFirst().orElseThrow();assertFalse(((List<?>)trace.get("dependsOn")).stream().map(String::valueOf).anyMatch(x->x.startsWith("b-6:")));
 }

 @Test void internalMarkdownEmptyCellsNeverShiftParticipantColumns(){var p=parser.parse("x.md","|シナリオ名|ゲームシステム|PL|PC|\n|---|---|---|---|\n|A||人物|PC|").getFirst();assertNull(p.data().get("gameSystemName"));assertEquals("人物",list(p.data().get("participations")).getFirst().get("personName"));assertEquals("PC",list(p.data().get("participations")).getFirst().get("pcName"));}
 @Test void unqualifiedPcWithTwoPeopleIsRetainedInsteadOfBeingAttachedByListingOrder(){var p=parser.parse("x.csv","シナリオ名,記録,氏名,氏名,PC\nA,実施済み参加者記録,人物1,人物2,使用PC").getFirst();assertEquals(2,list(p.data().get("participations")).size());assertTrue(list(p.data().get("participations")).stream().allMatch(part->part.get("pcName")==null&&part.get("role")==null));assertEquals("使用PC",list(p.data().get("unassignedValues")).getFirst().get("rawValue"));}
 @Test void onePlAmongKpAndPlIsAQualifiedPcTarget(){var p=parser.parse("x.csv","シナリオ名,PL,KP,PC\nA,人物PL,人物KP,PC").getFirst();var parts=list(p.data().get("participations"));assertEquals("PC",parts.stream().filter(x->"PL".equals(x.get("role"))).findFirst().orElseThrow().get("pcName"));assertNull(parts.stream().filter(x->"KP".equals(x.get("role"))).findFirst().orElseThrow().get("pcName"));}
 @Test void onlyExplicitRecordedYearsCanNormalizeCalendarSeparators(){for(String date:List.of("2026/10/14","2026年10月14日","２０２６．１０．１４")){var p=parser.parse("x.txt","日付: "+date).getFirst();assertEquals("2026-10-14",list(p.data().get("tableDates")).getFirst().get("playedOn"));}assertTrue(list(parser.parse("x.txt","日付: 10/14").getFirst().data().get("tableDates")).isEmpty());}
 @Test void scenarioTitleIsNotAnActualParticipationContext(){var p=parser.parse("x.md","# シナリオ: プレイ済みという名前\n氏名: 人物\nPC: PC").getFirst();assertNull(list(p.data().get("participations")).getFirst().get("role"));}
 @Test void optionalFieldsFromAllFiveFormatsHaveSameMeaningAndExactFieldTrace(){
  String[] labels={"シナリオ名","作者","シナリオURL","PL","PC","HO","Quote","終了SAN","Character Sheet URL"};String[] values={"A","作者","https://example.com/scenario","人","青い月","HO1","まだ帰らない","53","https://example.com/pc"};String csv=String.join(",",labels)+"\n"+String.join(",",values),md="|"+String.join("|",labels)+"|\n|"+String.join("|",Collections.nCopies(labels.length,"---"))+"|\n|"+String.join("|",values)+"|";StringBuilder html=new StringBuilder("<table><tr>"),txt=new StringBuilder();for(String label:labels)html.append("<th>").append(label).append("</th>");html.append("</tr><tr>");for(String value:values)html.append("<td>").append(value).append("</td>");html.append("</tr></table>");for(int i=0;i<labels.length;i++)txt.append(labels[i]).append(": ").append(values[i]).append('\n');
  for(var c:Map.of("a.csv",csv,"a.md",md,"a.html",html.toString(),"a.txt",txt.toString(),"paste",txt.toString()).entrySet()){var p=parser.parse(c.getKey().equals("paste")?null:c.getKey(),c.getValue()).getFirst();assertEquals("作者",p.data().get("authorName"),c.getKey());assertEquals("https://example.com/scenario",p.data().get("sourceUrl"));var part=list(p.data().get("participations")).getFirst();assertEquals("まだ帰らない",part.get("displayQuote"));assertEquals("HO1",part.get("handout"));assertEquals(53,((Number)obj(obj(part.get("endPcState")).get("statusValues")).get("san")).intValue());assertTrue(list(p.data().get("unassignedValues")).isEmpty(),c.getKey());for(String field:List.of("authorName","sourceUrl","displayQuote","characterSheetUrl","endPcState/statusValues/san"))assertTrue(p.traces().stream().anyMatch(t->text(t.get("fieldPath")).endsWith("/"+field)),c.getKey()+field);}
 }
 @Test void explicitInlineMarkersSplitOnlyKnownFieldsNotNameSlashes(){var p=parser.parse("a.txt","シナリオ: A\nPL：田中 / α／PC：青い月／HO：HO1／セリフ：まだ帰らない／終了SAN：53").getFirst();var part=list(p.data().get("participations")).getFirst();assertEquals("田中 / α",part.get("personName"));assertEquals("青い月",part.get("pcName"));assertEquals("まだ帰らない",part.get("displayQuote"));}
 @Test void mainKpDoesNotTurnPlannedSkpIntoActualKp(){var p=parser.parse("a.csv","シナリオ名,記録,KP,SKP\nA,実施済み,主,\nA,予定,,補助");assertNull(list(p.getLast().data().get("participations")).getFirst().get("role"));}

}
