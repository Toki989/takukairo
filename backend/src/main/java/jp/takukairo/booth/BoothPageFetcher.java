package jp.takukairo.booth;

import jp.takukairo.common.ApiException;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;

/** Only fetches reconstructed BOOTH product URLs. No cookies, credentials or automatic redirects. */
final class BoothPageFetcher {
 static final int MAX_BYTES=2*1024*1024;
 private final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NEVER).build();

 String fetch(String productId) {
  if(!productId.matches("[0-9]+"))throw new IllegalArgumentException("Invalid product id");
  URI target=URI.create("https://booth.pm/ja/items/"+productId);
  long deadline=System.nanoTime()+Duration.ofSeconds(20).toNanos();
  for(int redirects=0;redirects<=3;redirects++) {
   long remaining=deadline-System.nanoTime();if(remaining<=0)throw timeout();
   var request=HttpRequest.newBuilder(target).timeout(Duration.ofNanos(remaining))
    .header("User-Agent","Takukairo/0.1 (user-requested BOOTH product preview)")
    .header("Accept","text/html,application/xhtml+xml").header("Accept-Encoding","identity").GET().build();
   var pending=client.sendAsync(request,info->new LimitedBody());
   try {
    var response=pending.get(remaining,TimeUnit.NANOSECONDS);
    int status=response.statusCode();
    if(List.of(301,302,303,307,308).contains(status)) {
     String location=response.headers().firstValue("location").orElse("");
     URI next=target.resolve(location);
     // A login page, another product, or another host is never followed.
     if(location.isBlank()||redirects==3||!"https".equalsIgnoreCase(next.getScheme())||!"booth.pm".equalsIgnoreCase(next.getHost())
       ||next.getUserInfo()!=null||(next.getPort()!=-1&&next.getPort()!=443)
       ||!next.getPath().matches("/(?:ja/)?items/"+productId)||next.equals(target))throw unavailable();
     target=next;continue;
    }
    if(status!=200)throw new ApiException(502,"BOOTH_FETCH_FAILED","BOOTHの商品情報を取得できませんでした（HTTP "+status+"）。URLを確認するか、手入力で続けてください。");
    String type=response.headers().firstValue("content-type").orElse("").toLowerCase(java.util.Locale.ROOT);
    if(!type.startsWith("text/html")&&!type.startsWith("application/xhtml+xml"))throw unavailable();
    if(response.headers().firstValue("content-encoding").filter(v->!v.equalsIgnoreCase("identity")).isPresent())throw unavailable();
    return new String(response.body(),StandardCharsets.UTF_8);
   }catch(TimeoutException e){pending.cancel(true);throw timeout();}
   catch(InterruptedException e){pending.cancel(true);Thread.currentThread().interrupt();throw timeout();}
   catch(ExecutionException e){if(e.getCause() instanceof ApiException failure)throw failure;if(e.getCause() instanceof HttpTimeoutException)throw timeout();throw unavailable();}
   catch(IllegalArgumentException e){throw unavailable();}
  }
  throw unavailable();
 }
 static ApiException unavailable(){return new ApiException(502,"BOOTH_FETCH_FAILED","BOOTHの商品情報を取得できませんでした。時間をおいて再試行するか、手入力で続けてください。");}
 private static ApiException timeout(){return new ApiException(504,"BOOTH_FETCH_TIMEOUT","BOOTHへの接続が時間切れになりました。入力内容は変更していません。もう一度取得するか、手入力で続けてください。");}

 /** Cancel while streaming, rather than buffering an unbounded response before checking its size. */
 static final class LimitedBody implements HttpResponse.BodySubscriber<byte[]> {
  private final CompletableFuture<byte[]> result=new CompletableFuture<>();
  private final ByteArrayOutputStream bytes=new ByteArrayOutputStream();
  private Flow.Subscription subscription;
  public CompletionStage<byte[]> getBody(){return result;}
  public void onSubscribe(Flow.Subscription value){subscription=value;value.request(1);}
  public void onNext(List<ByteBuffer> buffers){
   if(result.isDone())return;
   for(var buffer:buffers){if(buffer.remaining()>MAX_BYTES-bytes.size()){subscription.cancel();result.completeExceptionally(new ApiException(502,"BOOTH_RESPONSE_TOO_LARGE","BOOTHの応答が大きすぎるため取得を中止しました。手入力で続けてください。"));return;}byte[] chunk=new byte[buffer.remaining()];buffer.get(chunk);bytes.writeBytes(chunk);}
   subscription.request(1);
  }
  public void onError(Throwable error){result.completeExceptionally(error);}
  public void onComplete(){result.complete(bytes.toByteArray());}
 }
}
