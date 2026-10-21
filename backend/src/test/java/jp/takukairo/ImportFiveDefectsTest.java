package jp.takukairo;

import jp.takukairo.importsession.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static jp.takukairo.common.Json.*;

/** Internal regression cases, not the user's T01-T24 acceptance fixtures. */
class ImportFiveDefectsTest {
 @Test void markdownQuoteSpansIncludeLazyNestedAndCodeContinuationsButStopAtNormalRecords(){
  for(String quote:List.of("> シナリオ: 参考例\nPL: 引用人物\nPC: 青い月","> > ネスト引用\nPL: 引用人物\nPC: 青い月","> ```\n> PL: 引用人物\n> ```","> シナリオ: 参考例\r\nPL: 引用人物\r\nPC: 青い月")){
   String raw=quote+"\n\nシナリオ: 通常\nPL: 実参加\nPC: 実PC";var parsed=new ImportParser().parse("quote.md",raw);
   assertEquals(List.of("実参加"),parsed.stream().flatMap(p->list(p.data().get("participations")).stream()).map(p->p.get("personName")).toList(),quote);
   assertTrue(parsed.stream().flatMap(p->p.traces().stream()).anyMatch(t->text(t.get("excerpt")).contains("PL: 引用人物")),quote);
  }
 }
 @Test void roleAlternativesClearProvisionalValueWithoutErasingEvidence(){
  var p=new ImportParser().parse("roles.csv","シナリオ名,PL,役割\nA,田中,KP/PL").getFirst();var person=list(p.data().get("participations")).getFirst();assertNull(person.get("role"));assertTrue(list(p.inference().get("relations")).stream().anyMatch(r->"ALTERNATIVES".equals(r.get("state"))));assertTrue(p.traces().stream().anyMatch(t->"PL".equals(t.get("excerpt"))||text(t.get("excerpt")).contains("田中")));
 }
 @Test void emptyStateDefinitionIgnoresMetadataButRetainsActualData(){
  for(var state:List.of(map(),map("statusValues",map()),map("profileKey","coc_7e_v1","statusValues",map("san",null),"growth"," ","outcome",null,"aftereffects","")))assertFalse(ImportDrafts.hasState(state));
  for(var state:List.of(map("statusValues",map("san",0)),map("growth","成長"),map("outcome","LOST"),map("aftereffects","後遺症")))assertTrue(ImportDrafts.hasState(state));
 }
}
