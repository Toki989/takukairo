package jp.takukairo.gamesystem;
import jp.takukairo.common.*;
import jp.takukairo.security.*;
import jp.takukairo.model.*;
import static jp.takukairo.common.Json.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.*;
@Service
public class CharacterPreviewService {
 private final Profiles profiles;private final CcfoliaPawnAdapter adapter;private final CurrentUser user;private final CcfoliaMappingConfig config;
 public CharacterPreviewService(Profiles p,CcfoliaPawnAdapter a,CurrentUser u,CcfoliaMappingConfig c){profiles=p;adapter=a;user=u;config=c;}
 @Transactional(readOnly=true) public Object preview(Map<String,Object>b){
  if(!(b.get("rawText") instanceof String raw))throw ApiException.invalid("/rawText","REQUIRED","駒データを入力してください。");
  if(raw.getBytes(StandardCharsets.UTF_8).length>2_097_152)throw ApiException.invalid("/rawText","TOO_LARGE","貼り付けテキストは2MB以下にしてください。");
  var source=adapter.parse(new CharacterSourceAdapter.TextPayload(raw));Long sid=Json.id(b.get("scenarioId"));String system=sid==null?null:user.owned(Scenario.class,sid).gameSystem;var target=profiles.resolveProfile(system);
  String memo=source.memoText()==null?"":source.memoText();var evidence=config.detectedProfiles(memo);boolean ambiguous=evidence.size()>1;Map<String,Object> detected=evidence.size()==1?profiles.get(evidence.getFirst()):null;
  Map<String,Object> preview=target!=null?target:detected;String origin=target!=null?"SCENARIO":detected!=null?"INFERRED":"UNKNOWN";
  if(b.containsKey("manualProfileKey")){try{preview=profiles.get(text(b.get("manualProfileKey")));if(!Boolean.TRUE.equals(preview.get("active")))throw new Exception();origin="MANUAL";}catch(Exception e){throw ApiException.invalid("/manualProfileKey","INVALID_PROFILE","現在利用できる設定形式を選択してください。");}}
  var warnings=new ArrayList<Object>();boolean mismatch=target!=null&&detected!=null&&!target.get("profileKey").equals(detected.get("profileKey"))||target!=null&&preview!=null&&!target.get("profileKey").equals(preview.get("profileKey"));
  if(ambiguous)warnings.add(map("code","PROFILE_AMBIGUOUS","path",null,"message","複数の設定形式を示す記載があります。設定形式を確認してください。"));
  if(mismatch)warnings.add(map("code","PROFILE_MISMATCH","path",null,"message","シナリオのゲームシステムと駒データの候補が一致しません。"));
  if(system!=null&&!system.isBlank()&&target==null)warnings.add(map("code","UNSUPPORTED_GAME_SYSTEM","path",null,"message","このゲームシステムの状態入力補助には対応していません。手入力を続けられます。"));
  var values=new LinkedHashMap<String,Object>();var fields=new LinkedHashMap<String,Object>();
  if(preview!=null&&!ambiguous){var mapping=config.forProfile(text(preview.get("profileKey")));
   for(var entry:obj(mapping.get("statusMappings")).entrySet()){String label=Profiles.normalize(entry.getKey()).toUpperCase(Locale.ROOT);var matching=source.statuses().stream().filter(v->Profiles.normalize(text(v.get("label"))).toUpperCase(Locale.ROOT).equals(label)).toList();if(matching.size()==1){Object v=matching.getFirst().get("value");if(v instanceof Integer||v instanceof Long)values.put(text(entry.getValue()),v);}}
   for(var entry:obj(mapping.get("memoMappings")).entrySet()){String sourceKey=entry.getKey(),fieldKey=text(entry.getValue());var lines=memo.lines().map(Profiles::normalize).filter(l->l.startsWith(sourceKey+":")).toList();if(lines.size()>1){warnings.add(map("code","DUPLICATE_MEMO_KEY","path","/pcCandidate/systemFields/"+fieldKey,"message","共鳴感情の記載が複数あります。確認してください。"));continue;}
    if(lines.size()==1){String value=Profiles.normalize(lines.getFirst().substring(sourceKey.length()+1));for(var options:list(preview.get("optionSources")))for(var option:list(options.get("options")))if(Profiles.normalize(text(option.get("label"))).equals(value))fields.put(fieldKey,option.get("key"));}
   }
  }
  String url=source.referenceUrlCandidate();if(url!=null&&!url.isBlank())try{url=Validation.url(url,"/pcCandidate/characterSheetUrl");}catch(ApiException e){url=null;warnings.add(map("code","INVALID_EXTERNAL_URL","path","/pcCandidate/characterSheetUrl","message","キャラクターシートURLを確認してください。"));}
  boolean applicable=target!=null&&preview!=null&&target.get("profileKey").equals(preview.get("profileKey"))&&!mismatch&&!ambiguous;
  return map("targetSystem",sid==null?null:map("gameSystem",system,"canonicalSystemKey",target==null?null:target.get("canonicalSystemKey"),"profileKey",target==null?null:target.get("profileKey"),"supported",target!=null),"detectedProfile",detected==null?null:map("profileKey",detected.get("profileKey"),"canonicalSystemKey",detected.get("canonicalSystemKey"),"confidence","HIGH"),"previewProfile",map("profileKey",preview==null?null:preview.get("profileKey"),"canonicalSystemKey",preview==null?null:preview.get("canonicalSystemKey"),"source",origin,"confidence",preview==null?"LOW":"HIGH"),"endPcStateCandidate",map("statusValues",values,"applicable",applicable),"pcCandidate",map("nameCandidate",source.nameCandidate(),"systemFields",fields,"characterSheetUrl",url,"iconUrl",source.imageSourceCandidate()),"warnings",warnings);
 }
}
