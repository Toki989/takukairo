package jp.takukairo.gamesystem;
public class TekeyCharacterAdapter implements CharacterSourceAdapter {
 public SourceKind sourceKind(){return SourceKind.TEKEY;}public boolean supports(SourcePayload p){return false;}public ParsedCharacterSource parse(SourcePayload p){throw new UnsupportedOperationException("Extension point only; concrete contract deferred");}
}
