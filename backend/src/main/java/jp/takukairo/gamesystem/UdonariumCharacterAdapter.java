package jp.takukairo.gamesystem;
public class UdonariumCharacterAdapter implements CharacterSourceAdapter {
 public SourceKind sourceKind(){return SourceKind.UDONARIUM;}public boolean supports(SourcePayload p){return false;}public ParsedCharacterSource parse(SourcePayload p){throw new UnsupportedOperationException("Extension point only; concrete contract deferred");}
}
