package jp.takukairo.booth;

import jp.takukairo.common.ApiException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Flow;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static jp.takukairo.common.Json.*;

class LiveBoothAdapterTest {
 private String page(String name,String description){return "<script type=\"application/ld+json\">"+write(map("@type","Product","name",name,"description",description,"url","https://booth.pm/ja/items/3403696","brand",map("name","ショップ"),"image","https://booth.pximg.net/c/620x620/test/i/3403696/cropped.jpg"))+"</script>";}
 @Test void extractsProductCreditEditionAndOriginalImageRatherThanCroppedSocialImage(){
  var result=LiveBoothAdapter.parse("3403696",page("鰯と柊【クトゥルフ神話TRPG/6版】","シナリオ：○助\nNPC立ち絵：別の人\nCopyright 2019")+"<img class=market-item-detail-item-image data-origin='https://booth.pximg.net/test/i/3403696/original.png' src='https://booth.pximg.net/test/i/3403696/resized.jpg'>");
  assertEquals("鰯と柊【クトゥルフ神話TRPG/6版】",result.get("name"));assertEquals("○助",result.get("authorName"));assertEquals("クトゥルフ神話TRPG 6版",result.get("gameSystem"));assertEquals("https://booth.pximg.net/test/i/3403696/original.png",result.get("externalImageUrl"));assertEquals("https://booth.pm/ja/items/3403696",result.get("sourceUrl"));
 }
 @Test void doesNotInventEditionFromGenericTitleOrUnrelatedCopyright(){var result=LiveBoothAdapter.parse("3403696",page("クトゥルフ神話TRPG シナリオ","第7版の著作権についての説明"));assertNull(result.get("gameSystem"));assertNull(result.get("authorName"));}
 @Test void handlesGraphAndMissingImageAndFallsBackToShopOnlyAsCandidate(){String html="<script type='application/ld+json'>"+write(map("@graph",List.of(map("@type","Product","name","エモクロアTRPG シナリオ","url","https://booth.pm/ja/items/3403696"))))+"</script><div class='shop-name'><a>ショップ名</a></div>";var result=LiveBoothAdapter.parse("3403696",html);assertEquals("ショップ名",result.get("authorName"));assertEquals("エモクロアTRPG",result.get("gameSystem"));assertNull(result.get("externalImageUrl"));}
 @Test void rejectsLoginAndAnotherProductAndDoesNotReturnArbitraryImageHost(){assertThrows(ApiException.class,()->LiveBoothAdapter.parse("3403696","<h1>ログインしてください</h1>"));assertThrows(ApiException.class,()->LiveBoothAdapter.parse("111",page("商品","")));var html=page("商品","").replace("https://booth.pximg.net/c/620x620/test/i/3403696/cropped.jpg","http://127.0.0.1/secret");assertNull(LiveBoothAdapter.parse("3403696",html).get("externalImageUrl"));}
 @Test void controllerReconstructsCanonicalUrlAndPreservesFetchErrors(){var controller=new BoothController((id,url)->map("id",id,"url",url));var response=obj(controller.preview(map("url","https://shop.booth.pm/items/3403696?tracking=1#fragment")));assertEquals("3403696",response.get("id"));assertEquals("https://booth.pm/ja/items/3403696",response.get("url"));for(String url:List.of("https://booth.pm.evil.test/items/3403696","https://example.com/items/3403696","https://user@booth.pm/items/3403696","https://booth.pm/ja/items/3403696/other"))assertThrows(ApiException.class,()->controller.preview(map("url",url)));var failing=new BoothController((id,url)->{throw new ApiException(504,"BOOTH_FETCH_TIMEOUT","timeout");});assertEquals("BOOTH_FETCH_TIMEOUT",assertThrows(ApiException.class,()->failing.preview(map("url","https://booth.pm/ja/items/3403696"))).code);}
 @Test void stopsOversizedStreamingResponseBeforeKeepingItInMemory(){var body=new BoothPageFetcher.LimitedBody();boolean[] cancelled={false};body.onSubscribe(new Flow.Subscription(){public void request(long n){}public void cancel(){cancelled[0]=true;}});body.onNext(List.of(ByteBuffer.wrap(new byte[BoothPageFetcher.MAX_BYTES]),ByteBuffer.wrap(new byte[1])));assertTrue(cancelled[0]);assertTrue(body.getBody().toCompletableFuture().isCompletedExceptionally());}
 @Test void acceptsBoundedBody(){var body=new BoothPageFetcher.LimitedBody();body.onSubscribe(new Flow.Subscription(){public void request(long n){}public void cancel(){fail("unexpected cancel");}});body.onNext(List.of(ByteBuffer.wrap(new byte[]{1,2}),ByteBuffer.wrap(new byte[]{3})));body.onComplete();assertArrayEquals(new byte[]{1,2,3},body.getBody().toCompletableFuture().join());}
}
