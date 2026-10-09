package jp.takukairo.gamesystem;
import org.springframework.stereotype.Component;
import jp.takukairo.common.*;
import static jp.takukairo.common.Json.*;
import java.util.*;
@Component
public class CcfoliaPawnAdapter implements CharacterSourceAdapter {
 public SourceKind sourceKind(){return SourceKind.CCFOLIA;}
 public boolean supports(SourcePayload p){return p instanceof TextPayload;}
 public ParsedCharacterSource parse(SourcePayload p){if(!(p instanceof TextPayload t))throw ApiException.invalid("/rawText","UNSUPPORTED_CCFOLIA_PAYLOAD","駒形式JSONを貼り付けてください。");var root=object(t.text());if(!"character".equals(root.get("kind")))throw ApiException.invalid("/rawText","UNSUPPORTED_CCFOLIA_PAYLOAD","ココフォリア駒形式を貼り付けてください。");var d=obj(root.get("data"));return new ParsedCharacterSource(sourceKind(),text(d.get("name")),text(d.get("externalUrl")),text(d.get("iconUrl")),list(d.get("status")),list(d.get("params")),text(d.get("memo")),d.get("commands"),d.get("initiative"),List.of());}
}
