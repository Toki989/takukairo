package jp.takukairo.booth;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.net.URI;
import jp.takukairo.common.*;
@RestController
public class BoothController {
 private final BoothAdapter adapter;public BoothController(BoothAdapter a){adapter=a;}
 @PostMapping("/api/booth/preview") Object preview(@RequestBody Map<String,Object>b){String url=Validation.url(b.get("url"),"/url");try{var u=URI.create(url);if(!u.getHost().equals("booth.pm")&&!u.getHost().endsWith(".booth.pm"))throw new Exception();var matcher=java.util.regex.Pattern.compile("/(?:ja/)?items/([0-9]+)").matcher(u.getPath());if(!matcher.matches())throw new Exception();String id=matcher.group(1);return adapter.preview(id,"https://booth.pm/ja/items/"+id);}catch(Exception e){throw ApiException.invalid("/url","INVALID_BOOTH_URL","BOOTHの商品URLを入力してください。");}}
}
