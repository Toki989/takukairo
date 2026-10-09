package jp.takukairo.booth;
import org.springframework.stereotype.Component;
import java.util.*;
import static jp.takukairo.common.Json.*;
@Component
public class MockBoothAdapter implements BoothAdapter {
 public Map<String,Object> preview(String productId,String url){return map("name","テスト用シナリオ "+productId,"authorName","テスト用作者","gameSystem","クトゥルフ神話TRPG 7版","externalImageUrl",null,"sourceUrl",url);}
}
