package jp.takukairo.booth;

import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import jp.takukairo.common.Json;
import java.net.URI;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import static jp.takukairo.common.Json.*;
import static jp.takukairo.common.Validation.optional;

@Component
@ConditionalOnProperty(name="app.booth.live-fetch",havingValue="true")
public class LiveBoothAdapter implements BoothAdapter {
 private final BoothPageFetcher fetcher=new BoothPageFetcher();
 public Map<String,Object> preview(String productId,String sourceUrl){return parse(productId,fetcher.fetch(productId));}

 static Map<String,Object> parse(String productId,String html) {
  var doc=Jsoup.parse(html);Map<String,Object> product=null;
  for(var script:doc.select("script[type=application/ld+json]")){
   try{product=findProduct(Json.MAPPER.readValue(script.data(),Object.class),productId);if(product!=null)break;}catch(Exception ignored){}
  }
  String name=product==null?null:optional(product.get("name"));
  if(name==null){var heading=doc.selectFirst(".market-item-detail-item-title");if(heading!=null)name=optional(heading.text());}
  if(name==null)throw BoothPageFetcher.unavailable();
  String description=product==null?null:optional(product.get("description"));
  if(description==null){var content=doc.selectFirst(".market-item-detail-description");description=content==null?"":content.wholeText();}
  String author=null;
  if(product!=null){Object value=product.get("author");author=value instanceof String text?text:optional(obj(value).get("name"));}
  var credit=Pattern.compile("(?m)^\\h*(?:シナリオ(?:作者|制作)?|作者|著者)\\h*[:：]\\h*([^\\r\\n]+)").matcher(description);
  if(author==null&&credit.find())author=credit.group(1).strip();
  if(author==null){var shop=doc.selectFirst(".shop-name a");if(shop!=null)author=optional(shop.text());}
  String image=null;
  for(var img:doc.select(".market-item-detail-item-image")){
   for(String attr:List.of("data-origin","src","data-lazy")){image=imageUrl(img.attr(attr),productId);if(image!=null)break;}
   if(image!=null)break;
  }
  if(image==null&&product!=null){Object value=product.get("image");if(value instanceof List<?> values&&!values.isEmpty())value=values.getFirst();image=imageUrl(value instanceof String text?text:optional(obj(value).get("url")),productId);}
  return map("name",name,"authorName",author,"gameSystem",gameSystem(name,description),"externalImageUrl",image,"sourceUrl","https://booth.pm/ja/items/"+productId);
 }
 private static Map<String,Object> findProduct(Object value,String id){
  if(value instanceof List<?> values){for(Object child:values){var found=findProduct(child,id);if(found!=null)return found;}}
  if(value instanceof Map<?,?>){var node=obj(value);Object type=node.get("@type");if("Product".equals(type)||type instanceof List<?> types&&types.contains("Product")){
   String url=optional(node.get("url"));if(url!=null){try{URI u=URI.create(url);if(!"booth.pm".equalsIgnoreCase(u.getHost())||!u.getPath().matches("/(?:ja/)?items/"+id))return null;}catch(Exception e){return null;}}
   return node;
  }return findProduct(node.get("@graph"),id);}
  return null;
 }
 private static String imageUrl(String value,String id){if(value==null||value.isBlank()||value.length()>2048)return null;try{URI u=URI.create(value);return "https".equalsIgnoreCase(u.getScheme())&&"booth.pximg.net".equalsIgnoreCase(u.getHost())&&u.getUserInfo()==null&&(u.getPort()==-1||u.getPort()==443)&&u.getPath().contains("/i/"+id+"/")?u.toASCIIString():null;}catch(Exception e){return null;}}
 private static String gameSystem(String title,String description){
  String result=system(title);if(result!=null)return result;
  var field=Pattern.compile("(?m)^\\h*(?:ゲームシステム|システム|対応ルール|ルールブック)\\h*[:：]\\h*([^\\r\\n]+)").matcher(description);
  return field.find()?system(field.group(1)):null;
 }
 private static String system(String value){String text=Normalizer.normalize(value,Normalizer.Form.NFKC);if(text.contains("エモクロア"))return "エモクロアTRPG";if(!Pattern.compile("クトゥルフ|\\bCoC\\b|\\bCoC[67]",Pattern.CASE_INSENSITIVE).matcher(text).find())return null;boolean six=Pattern.compile("6\\s*版|第\\s*6\\s*版|CoC\\s*6(?:th)?\\b",Pattern.CASE_INSENSITIVE).matcher(text).find(),seven=Pattern.compile("7\\s*版|第\\s*7\\s*版|CoC\\s*7(?:th)?\\b|新クトゥルフ",Pattern.CASE_INSENSITIVE).matcher(text).find();return six==seven?null:six?"クトゥルフ神話TRPG 6版":"クトゥルフ神話TRPG 7版";}
}
