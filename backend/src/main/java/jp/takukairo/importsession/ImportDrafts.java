package jp.takukairo.importsession;
import jp.takukairo.common.*;
import jp.takukairo.gamesystem.Profiles;
import java.util.*;
import static jp.takukairo.common.Json.*;
import static jp.takukairo.common.Validation.*;

/** Draft-only decisions and validation. Formal services remain the final save boundary. */
public final class ImportDrafts {
 private ImportDrafts() {}
 public static final Set<String> SCENARIO_OPTIONAL=Set.of("authorName","sourceUrl");
 public static final Set<String> PART_OPTIONAL=Set.of("handout","displayQuote","endPcState","characterSheetUrl");
 public static void applyAssignments(Map<String,Object>d){
  var assigned=new HashMap<String,Object>();for(var value:list(d.get("unassignedValues"))){var a=obj(value.get("assignment"));String type=text(a.get("type"));
   if(!Set.of("UNASSIGNED","PARTICIPATION_PERSON","PARTICIPATION_PC","PARTICIPATION_HANDOUT","IGNORE").contains(type==null?"":type))throw ApiException.invalid("/candidateData/unassignedValues","INVALID_ASSIGNMENT","値の扱いを確認してください。");
   if(!type.startsWith("PARTICIPATION_"))continue;
   var part=list(d.get("participations")).stream().filter(p->Objects.equals(p.get("candidateParticipationKey"),a.get("candidateParticipationKey"))).findFirst().orElseThrow(()->ApiException.invalid("/candidateData/unassignedValues","INVALID_ASSIGNMENT","割り当てる参加者を選択してください。"));
   String field=switch(type){case "PARTICIPATION_PERSON"->"personName";case "PARTICIPATION_PC"->"pcName";default->"handout";};
   if("OMIT".equals(part.get("saveDecision"))||omitted(part,field))continue;
   String target=text(part.get("candidateParticipationKey"))+":"+field;if(assigned.containsKey(target)&&!Objects.equals(assigned.get(target),value.get("rawValue")))throw ApiException.invalid("/candidateData/unassignedValues","CONFLICTING_ASSIGNMENTS","同じ項目に異なる値が割り当てられています。原文を確認してください。");assigned.put(target,value.get("rawValue"));part.put(field,value.get("rawValue"));
  }
 }
 public static Map<String,Object> assigned(Map<String,Object>d){var result=object(write(d));applyAssignments(result);return result;}
 public static Map<String,Object> effective(Map<String,Object> original) {
  var d=object(write(original));validateOmissions(d,SCENARIO_OPTIONAL);
  omit(d);var parts=new ArrayList<Map<String,Object>>();var omittedParts=new HashSet<String>();
  for(var p:list(d.get("participations"))){validateOmissions(p,PART_OPTIONAL);String choice=text(p.get("saveDecision"));if(choice!=null&&!Set.of("SAVE","OMIT","HOLD").contains(choice))throw ApiException.invalid("/candidateData/participations/saveDecision","INVALID_OPTION","参加記録の扱いを選択してください。");if("OMIT".equals(choice)){omittedParts.add(text(p.get("candidateParticipationKey")));continue;}if("HOLD".equals(choice))throw ApiException.invalid("/candidateData/participations/saveDecision","UNRESOLVED_ROLE","参加記録が保留されています。");omit(p);parts.add(p);}d.put("participations",parts);d.put("unassignedValues",list(d.get("unassignedValues")).stream().filter(v->!omittedParts.contains(text(obj(v.get("assignment")).get("candidateParticipationKey")))).toList());return d;
 }
 private static void validateOmissions(Map<String,Object>d,Set<String> allowed){Object value=d.get("omittedFields");if(value!=null&&(!(value instanceof List<?> values)||values.stream().anyMatch(x->!(x instanceof String)||!allowed.contains(x))))throw ApiException.invalid("/candidateData/omittedFields","INVALID_OPTION","今回は保存しない項目を確認してください。");}
 private static void omit(Map<String,Object>d){if(d.get("omittedFields") instanceof List<?> values)for(var key:values)d.remove(String.valueOf(key));}
 public static List<Map<String,Object>> omissions(Map<String,Object>d){var out=new ArrayList<Map<String,Object>>();for(var key:SCENARIO_OPTIONAL)if(omitted(d,key))out.add(map("fieldPath","/table/"+key,"value",d.get(key),"decision","USER_OMIT"));for(var p:list(d.get("participations"))){String prefix="/participations/"+p.get("candidateParticipationKey");if("OMIT".equals(p.get("saveDecision")))out.add(map("fieldPath",prefix,"value",p,"decision","USER_OMIT"));else for(var key:PART_OPTIONAL)if(omitted(p,key))out.add(map("fieldPath",prefix+"/"+key,"value",p.get(key),"decision","USER_OMIT"));}for(var v:list(d.get("unassignedValues")))if("IGNORE".equals(obj(v.get("assignment")).get("type")))out.add(map("fieldPath","/unassignedValues/"+v.get("itemKey"),"value",v.get("rawValue"),"decision","USER_OMIT"));return out;}
 public static boolean omitted(Map<String,Object>d,String key){return d.get("omittedFields") instanceof List<?> values&&values.contains(key);}
 public static void validateOptional(Map<String,Object>d,String gameSystem,Profiles profiles) {
  url(d.get("sourceUrl"),"/candidateData/sourceUrl");
  for(var p:list(d.get("participations"))){String path="/candidateData/participations/"+p.get("candidateParticipationKey");
   String quote=exactQuote(p.get("displayQuote"));if(graphemes(quote)>24)throw ApiException.invalid(path+"/displayQuote","TOO_LONG","Quoteは24 Grapheme以下へ修正するか、今回は保存しないを選択してください。");
   if(optional(p.get("handout"))!=null&&quote!=null&&!Set.of("HO","QUOTE").contains(Optional.ofNullable(text(p.get("spotlightType"))).orElse("")))throw ApiException.invalid(path+"/spotlightType","SPOTLIGHT_REQUIRED","HOとQuoteのどちらを表示するか選択してください。");
   var state=obj(p.get("endPcState"));boolean hasState=hasState(state);
   if("KP".equals(p.get("role"))&&(quote!=null||hasState))throw ApiException.invalid(path,"INVALID_ROLE","KPではQuote・終了時データを保存できません。");
   if(hasState){if(optional(p.get("pcName"))==null)throw ApiException.invalid(path+"/endPcState","PC_REQUIRED","PCを設定するか、終了時データを今回は保存しないとしてください。");var profile=profiles.resolveProfile(gameSystem);String key=profile==null?null:text(profile.get("profileKey"));if(state.get("profileKey")!=null&&!Objects.equals(state.get("profileKey"),key))throw ApiException.invalid(path+"/endPcState/profileKey","PROFILE_MISMATCH","解決済みシナリオのProfileを確認してください。");profiles.validateState(key,obj(state.get("statusValues")),path+"/endPcState/statusValues");longText(state.get("growth"),path+"/endPcState/growth",500);longText(state.get("aftereffects"),path+"/endPcState/aftereffects",500);String outcome=optional(state.get("outcome"));if(outcome!=null&&!Set.of("SURVIVED","LOST").contains(outcome))throw ApiException.invalid(path+"/endPcState/outcome","INVALID_OPTION","結末を確認してください。");}
   if(optional(p.get("characterSheetUrl"))!=null){url(p.get("characterSheetUrl"),path+"/characterSheetUrl");if(optional(p.get("pcName"))==null||!Set.of("COMMON","SYSTEM").contains(Optional.ofNullable(text(p.get("sheetUrlTarget"))).orElse("")))throw ApiException.invalid(path+"/sheetUrlTarget","TARGET_REQUIRED","Character Sheet URLの保存先を選択してください。");if("SYSTEM".equals(p.get("sheetUrlTarget"))&&optional(gameSystem)==null)throw ApiException.invalid(path+"/sheetUrlTarget","SYSTEM_REQUIRED","保存先のゲームシステムを確認してください。");}
  }
 }
 public static boolean hasState(Map<String,Object>state){return hasEndPcState(state);}

 /** Auto-derived display choice must be visible before explicit reconfirmation. */
 public static boolean normalizeSpotlight(Map<String,Object>d){boolean changed=false;for(var p:list(d.get("participations"))){if(!p.containsKey("displayQuote")&&!p.containsKey("spotlightType"))continue;boolean ho=optional(p.get("handout"))!=null&&!omitted(p,"handout"),quote=exactQuote(p.get("displayQuote"))!=null&&!omitted(p,"displayQuote");if(ho&&quote)continue;Object desired=ho?"HO":quote?"QUOTE":null;if(!Objects.equals(p.get("spotlightType"),desired)){p.put("spotlightType",desired);changed=true;}}return changed;}
 public static Set<String> editedFields(Map<String,Object>before,Map<String,Object>after){var out=new LinkedHashSet<String>();Set<String> keys=new LinkedHashSet<>(before.keySet());keys.addAll(after.keySet());for(var key:keys){if(key.equals("participations")){var old=new HashMap<String,Map<String,Object>>();list(before.get(key)).forEach(p->old.put(text(p.get("candidateParticipationKey")),p));for(var p:list(after.get(key))){var prior=old.remove(text(p.get("candidateParticipationKey")));Set<String> fields=new LinkedHashSet<>(p.keySet());if(prior!=null)fields.addAll(prior.keySet());for(var f:fields)if(prior==null||!Objects.equals(prior.get(f),p.get(f)))out.add("/participations/"+p.get("candidateParticipationKey")+"/"+f);}old.keySet().forEach(k->out.add("/participations/"+k));}else if(!Objects.equals(before.get(key),after.get(key)))out.add("/table/"+key);}return out;}
}
