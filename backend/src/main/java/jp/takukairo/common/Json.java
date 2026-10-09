package jp.takukairo.common;
import com.fasterxml.jackson.databind.*;
import java.util.*;
public final class Json {
    public static final ObjectMapper MAPPER=new ObjectMapper();
    public static String write(Object value){try{return MAPPER.writeValueAsString(value);}catch(Exception e){throw new IllegalStateException(e);}}
    @SuppressWarnings("unchecked") public static Map<String,Object> object(String json){try{return MAPPER.readValue(json,LinkedHashMap.class);}catch(Exception e){throw ApiException.invalid("/rawText","INVALID_JSON","JSONを解析できません。");}}
    public static Map<String,Object> map(Object... values){Map<String,Object> m=new LinkedHashMap<>();for(int i=0;i<values.length;i+=2)m.put((String)values[i],values[i+1]);return m;}
    public static Long id(Object v){if(v==null)return null;if(v instanceof Integer||v instanceof Long)return ((Number)v).longValue();throw new ApiException(400,"MALFORMED_REQUEST","整数のID・Versionを確認してください。");}
    public static long requiredLong(Map<String,Object> m,String key){Long v=id(m.get(key));if(v==null)throw ApiException.invalid("/"+key,"REQUIRED","指定が必要です。");return v;}
    public static String text(Object v){return v==null?null:v.toString();}
    @SuppressWarnings("unchecked") public static Map<String,Object> obj(Object v){if(v==null)return new LinkedHashMap<>();if(v instanceof Map<?,?>)return (Map<String,Object>)v;throw malformedStructure();}
    @SuppressWarnings("unchecked") public static List<Map<String,Object>> list(Object v){if(v==null)return new ArrayList<>();if(v instanceof List<?> items&&items.stream().allMatch(item->item instanceof Map<?,?>))return (List<Map<String,Object>>)v;throw malformedStructure();}
    private static ApiException malformedStructure(){return new ApiException(400,"MALFORMED_REQUEST","送信する項目の構造を確認してください。");}
}
