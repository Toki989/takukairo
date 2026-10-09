package jp.takukairo.common;
import java.net.URI;
import java.util.regex.Pattern;
public final class Validation {
 public static String optional(Object v){if(v==null)return null;if(!(v instanceof String))throw ApiException.invalid("/","INVALID_TYPE","文字列を入力してください。");String s=((String)v).trim();return s.isEmpty()?null:s;}
 public static String name(Object v,String path){String s=optional(v);if(s==null)throw ApiException.invalid(path,"REQUIRED","入力してください。");if(s.codePointCount(0,s.length())>255)throw ApiException.invalid(path,"TOO_LONG","255文字以下にしてください。");return s;}
 public static String url(Object v,String path){String s=optional(v);if(s==null)return null;try{URI u=new URI(s);if(s.length()>2048||!u.isAbsolute()||!(u.getScheme().equals("https")||u.getScheme().equals("http"))||u.getHost()==null||u.getUserInfo()!=null)throw new Exception();return s;}catch(Exception e){throw ApiException.invalid(path,"INVALID_URL","http / httpsのURLを入力してください。");}}
 public static int graphemes(String s){if(s==null)return 0;return (int)Pattern.compile("\\X").matcher(s).results().count();}
 public static String longText(Object v,String path,int max){String s=optional(v);if(s!=null&&s.codePointCount(0,s.length())>max)throw ApiException.invalid(path,"TOO_LONG",max+"文字以下にしてください。");return s;}
}
