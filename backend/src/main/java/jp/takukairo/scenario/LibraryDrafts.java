package jp.takukairo.scenario;
import jp.takukairo.common.*;
import jp.takukairo.gamesystem.Profiles;
import java.util.*;
import static jp.takukairo.common.Json.*;
import static jp.takukairo.common.Validation.*;

/** Constraints shared by Import preflight and new formal records. No writes. */
public final class LibraryDrafts {
 private LibraryDrafts() {}
 public static void scenario(Map<String,Object>d){
  if(d.containsKey("name"))name(d.get("name"),"/name");
  longText(d.get("authorName"),"/authorName",255);longText(d.get("gameSystem"),"/gameSystem",255);url(d.get("sourceUrl"),"/sourceUrl");
 }
 public static void newRecord(String type,Map<String,Object>d,Profiles profiles){
  name(d.get(type.equals("PERSON")?"displayName":"name"),"/draftData");
  if(type.equals("SCENARIO"))scenario(d);
  if(!type.equals("PC"))return;
  url(d.get("characterSheetUrl"),"/characterSheetUrl");
  if(d.get("systemSettings")!=null&&!(d.get("systemSettings") instanceof List<?>))throw ApiException.invalid("/systemSettings","INVALID_TYPE","設定一覧を確認してください。");
  var canonicalKeys=new HashSet<String>();int i=0;
  for(var x:list(d.get("systemSettings"))){String path="/systemSettings/"+i++;
   if(x.get("id")!=null)throw ApiException.invalid(path+"/id","INVALID_SETTING","新規PCに既存の設定は移動できません。");
   name(x.get("gameSystemName"),path+"/gameSystemName");url(x.get("characterSheetUrl"),path+"/characterSheetUrl");
   String canonical=optional(x.get("canonicalSystemKey")),key=optional(x.get("profileKey"));longText(canonical,path+"/canonicalSystemKey",128);longText(key,path+"/profileKey",128);
   if(canonical!=null&&!canonicalKeys.add(canonical))throw ApiException.invalid(path+"/canonicalSystemKey","DUPLICATE","同じゲームシステムは複数設定できません。");
   if(key!=null){Map<String,Object> def;try{def=profiles.get(key);}catch(ApiException e){throw ApiException.invalid(path+"/profileKey","PROFILE_MISMATCH","設定形式を確認してください。");}if(!Objects.equals(def.get("canonicalSystemKey"),canonical)||!Boolean.TRUE.equals(def.get("active")))throw ApiException.invalid(path+"/profileKey","PROFILE_MISMATCH","現在の設定形式を選択してください。");}
   if(x.get("profileValues")!=null&&!(x.get("profileValues") instanceof Map<?,?>))throw ApiException.invalid(path+"/profileValues","INVALID_TYPE","設定値を確認してください。");
   profiles.validateValues(key,obj(x.get("profileValues")),path+"/profileValues",Map.of());
  }
 }
}
