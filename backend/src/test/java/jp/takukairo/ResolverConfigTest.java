package jp.takukairo;
import org.junit.jupiter.api.Test;
import jp.takukairo.gamesystem.Profiles;
import java.util.*;
import static jp.takukairo.common.Json.*;
import static org.junit.jupiter.api.Assertions.*;
class ResolverConfigTest {
 @Test void initialAliasesEmptyAndExplicitTestAliasReturnsSuggestedWithoutCanonical()throws Exception{
  var profiles=new Profiles();var f=Profiles.class.getDeclaredField("resolver");f.setAccessible(true);var config=obj(f.get(profiles));assertTrue(list(config.get("aliases")).isEmpty());assertEquals("UNKNOWN",profiles.resolve("CoC").get("state"));assertEquals("EXACT",profiles.resolve("  クトゥルフ神話TRPG ７版  ").get("state"));assertEquals("EXACT",profiles.resolve("　クトゥルフ神話TRPG ７版　").get("state"));config.put("aliases",List.of(map("input","explicit test alias","canonicalSystemKeys",List.of("coc_6e","coc_7e"))));var result=profiles.resolve("explicit test alias");assertEquals("SUGGESTED",result.get("state"));assertNull(result.get("canonicalSystemKey"));assertNull(result.get("profileKey"));assertNull(result.get("displayName"));assertEquals(2,list(result.get("candidates")).size());assertEquals(Set.of("canonicalSystemKey","profileKey","displayName"),list(result.get("candidates")).getFirst().keySet());assertNull(profiles.resolveProfile("explicit test alias"));assertEquals("UNKNOWN",profiles.resolve("CoC").get("state"));
 }
}
