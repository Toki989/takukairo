package jp.takukairo.booth;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.net.URI;
import jp.takukairo.common.*;
@RestController
public class BoothController {
 private final BoothAdapter adapter;public BoothController(BoothAdapter a){adapter=a;}
 @PostMapping("/api/booth/preview") Object preview(@RequestBody Map<String,Object>b){String id;try{String url=Validation.url(b.get("url"),"/url");var u=URI.create(url);String host=u.getHost();if(host==null||(!host.equalsIgnoreCase("booth.pm")&&!host.toLowerCase(Locale.ROOT).endsWith(".booth.pm"))||u.getUserInfo()!=null)throw new IllegalArgumentException();var matcher=java.util.regex.Pattern.compile("/(?:ja/)?items/([0-9]+)").matcher(u.getPath());if(!matcher.matches())throw new IllegalArgumentException();id=matcher.group(1);}catch(IllegalArgumentException|NullPointerException e){throw ApiException.invalid("/url","INVALID_BOOTH_URL","BOOTHの商品URLを入力してください。");}return adapter.preview(id,"https://booth.pm/ja/items/"+id);}
}
