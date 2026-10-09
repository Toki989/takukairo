package jp.takukairo.gamesystem;
import java.util.*;
public interface CharacterSourceAdapter {
 enum SourceKind {CCFOLIA,TEKEY,UDONARIUM}
 sealed interface SourcePayload permits TextPayload,FilePayload {}
 record TextPayload(String text) implements SourcePayload{}
 record FilePayload(String name,byte[] content) implements SourcePayload{}
 record ParsedCharacterSource(SourceKind sourceKind,String nameCandidate,String referenceUrlCandidate,String imageSourceCandidate,List<Map<String,Object>> statuses,List<Map<String,Object>> parameters,String memoText,Object commands,Object initiative,List<Map<String,Object>> sourceWarnings){}
 SourceKind sourceKind();boolean supports(SourcePayload payload);ParsedCharacterSource parse(SourcePayload payload);
}
