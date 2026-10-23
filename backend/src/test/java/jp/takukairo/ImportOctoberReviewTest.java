package jp.takukairo;
import jp.takukairo.importsession.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.*;
import static jp.takukairo.common.Json.*;
import static org.junit.jupiter.api.Assertions.*;

class ImportOctoberReviewTest {
 @ParameterizedTest @ValueSource(strings={"プレイ済みではありません","実施済みではない","実施済みか未確認","参加者募集記録","参加者連絡記録","not played","never completed","unconfirmed","実施済みかどうか不明"})
 void r03OnlyAffirmativeActualContextInfersRole(String context){
  var parsed=new ImportParser().parse("x.csv","シナリオ名,記録,氏名,PC\nA,"+context+",田中,青い月").getFirst();var p=list(parsed.data().get("participations")).getFirst();assertEquals("田中",p.get("personName"));assertEquals("青い月",p.get("pcName"));assertNull(p.get("role"),context);
 }
 @Test void r03ChildUnknownStopsParentButSiblingRestoresContext(){
  var parsed=new ImportParser().parse("x.md","# シナリオ: A\n## 記録: 実施済み\n### 記録: 未確認\n氏名: 田中\nPC: 青い月\n### 記録: 実施済み\n氏名: 佐藤\nPC: 赤い星");
  var people=parsed.stream().flatMap(x->list(x.data().get("participations")).stream()).toList();assertEquals(2,people.size());assertNull(people.getFirst().get("role"));assertEquals("PL",people.getLast().get("role"));
 }
 @ParameterizedTest @ValueSource(strings={"completed","played","実施済み","プレイ済み"})
 void r03AffirmativeStillInfersPl(String context){var parsed=new ImportParser().parse("x.csv","シナリオ名,記録,氏名,PC\nA,"+context+",田中,青い月");assertEquals("PL",list(parsed.getFirst().data().get("participations")).getFirst().get("role"));}
 @ParameterizedTest @ValueSource(strings={
  "<table><tr><th>シナリオ名</th><th>PL</th><th>PC</th></tr><tr><td rowspan='2'>A</td><td>田中</td><td>青い月</td></tr><tr><td>佐藤</td><td>赤い星</td></tr></table>",
  "<table><tr><th>シナリオ名</th><th>PL</th><th>PC</th></tr><tr><td colspan='2'>A</td><td>青い月</td></tr></table>",
  "<table><tr><th>シナリオ名</th><th>PL</th></tr><tr><td>A</td><td><table><tr><th>PL</th><th>PC</th></tr><tr><td>田中</td><td>青い月</td></tr></table></td></tr></table>",
  "<table><tr><th>シナリオ名</th><th>PL</th></tr><tr><td>A</td></tr></table>",
  "<table><tr><th>シナリオ名</th><th>PL</th></tr><tr><th>A</th><th>田中</th></tr><tr><td>B</td><td>佐藤</td></tr></table>"})
 void r02UnsupportedGridRetainsWholeRangeWithoutFalseDirectFields(String raw){var parsed=new ImportParser().parse("x.html",raw);assertEquals(1,parsed.size());var piece=parsed.getFirst();assertTrue(list(piece.data().get("participations")).isEmpty());assertEquals("",piece.data().get("scenarioName"));assertEquals(raw,list(piece.data().get("unassignedValues")).getFirst().get("rawValue"));assertEquals(0,piece.inference().get("recordStartOffset"));assertEquals(raw.length(),piece.inference().get("recordEndOffset"));}
 @ParameterizedTest @ValueSource(strings={"<p>シナリオ: A</p><p>ゲームシステム: CoC</p><p>作者: 作者</p><p>シナリオURL: https://example.com/a</p><p>記録: 実施済み</p><table>","<table><caption>シナリオ: A</caption>"})
 void r04ImmediateContextTravelsWithFieldTrace(String prefix){String raw=prefix+"<tr><th>PL</th><th>PC</th></tr><tr><td>田中</td><td>青い月</td></tr></table><table><tr><th>PL</th></tr><tr><td>別卓</td></tr></table>";var parsed=new ImportParser().parse("x.html",raw);assertEquals(2,parsed.size());assertEquals("A",parsed.getFirst().data().get("scenarioName"));assertEquals("",parsed.getLast().data().get("scenarioName"));assertEquals("田中",list(parsed.getFirst().data().get("participations")).getFirst().get("personName"));assertTrue(parsed.getFirst().traces().stream().anyMatch(t->"/table/scenarioName".equals(t.get("fieldPath"))&&"INHERITED".equals(t.get("derivation"))&&text(t.get("excerpt")).contains("シナリオ: A")));}
 @ParameterizedTest @ValueSource(strings={"KP：佐藤、SKP：田中","SKP：田中、KP：佐藤","KP: 佐藤／SKP: 田中"})
 void r05InlineRolesPreserveMainOrder(String line){var piece=new ImportParser().parse(null,"シナリオ: A\n"+line).getFirst();var people=list(piece.data().get("participations"));assertEquals(List.of("佐藤","田中"),people.stream().map(p->p.get("personName")).toList());assertTrue(people.stream().allMatch(p->"KP".equals(p.get("role"))));}
 @Test void r05QuotedRoleLabelIsPartOfNameOrDialogue(){var piece=new ImportParser().parse(null,"シナリオ: A\nKP: 「佐藤、SKP：田中」\nPL: PL名\nQuote: 僕が言う、KP：逃げろ").getFirst();var people=list(piece.data().get("participations"));assertEquals(2,people.size());assertEquals("「佐藤、SKP：田中」",people.getFirst().get("personName"));assertEquals("僕が言う、KP：逃げろ",people.getLast().get("displayQuote"));}
 @Test void r05InlineQuoteRoleLabelCannotCreateAnotherParticipant(){var piece=new ImportParser().parse(null,"シナリオ: A\nPL: 田中／PC: 青い月／Quote: 僕が言う、KP：逃げろ").getFirst();var people=list(piece.data().get("participations"));assertEquals(1,people.size());assertEquals("僕が言う、KP：逃げろ",people.getFirst().get("displayQuote"));}
 @Test void r05RoleSegmentationPreservesExactQuoteBoundarySpaces(){var piece=new ImportParser().parse(null,"シナリオ: A\nKP: 佐藤、PL: 田中／PC: 青い月／Quote:  台詞  ").getFirst();var people=list(piece.data().get("participations"));assertEquals(2,people.size());assertEquals(" 台詞  ",people.getLast().get("displayQuote"));}
 @Test void r04MarkdownBlankBetweenParagraphAndTablePreservesSharedFields(){var parsed=new ImportParser().parse("x.md","シナリオ: A\nゲームシステム: CoC\n記録: 実施済み\n\n氏名 | PC\n--- | ---\n田中 | 青い月");assertEquals(1,parsed.size());assertEquals("A",parsed.getFirst().data().get("scenarioName"));assertEquals("CoC",parsed.getFirst().data().get("gameSystemName"));assertEquals("PL",list(parsed.getFirst().data().get("participations")).getFirst().get("role"));}
 @Test void r11MultilineSetextConsumesItsEntireHeadingRange(){String raw="シナリオ: A\r\nPL: 見出しの続き\r\n---\r\nPL: 実参加";var parsed=new ImportParser().parse("x.md",raw);assertEquals(1,parsed.size());assertEquals("A\nPL: 見出しの続き",parsed.getFirst().data().get("scenarioName"));assertEquals(List.of("実参加"),list(parsed.getFirst().data().get("participations")).stream().map(p->p.get("personName")).toList());assertTrue(parsed.getFirst().traces().stream().anyMatch(t->"/table/scenarioName".equals(t.get("fieldPath"))&&text(t.get("excerpt")).contains("見出しの続き\r\n---")));}
 @ParameterizedTest @ValueSource(strings={"# シナリオ: A\n---\nPL: 田中","シナリオ: A\n===\nPL: 田中","  |シナリオ名|PL|PC|\n  |---|---|---|\n  |A|田中|青い月|","シナリオ名 | PL | PC\n--- | --- | ---\nA | 田中 | 青い月"})
 void r11MarkdownStructureUsesActualHeadingKindAndTableColumns(String raw){var parsed=new ImportParser().parse("x.md",raw);assertEquals(1,parsed.size());assertEquals("A",parsed.getFirst().data().get("scenarioName"));assertEquals("田中",list(parsed.getFirst().data().get("participations")).getFirst().get("personName"));for(var trace:parsed.getFirst().traces())assertEquals(text(trace.get("excerpt")),raw.substring(((Number)trace.get("startOffset")).intValue(),((Number)trace.get("endOffset")).intValue()));}
}
