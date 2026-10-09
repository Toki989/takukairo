package jp.takukairo.security;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.*;
@Component @Order(10)
public class LargeTextGuard extends OncePerRequestFilter {
 public static final int LIMIT=16_777_216;
 protected void doFilterInternal(HttpServletRequest q,HttpServletResponse r,FilterChain chain)throws ServletException,IOException{
  String path=q.getRequestURI(),method=q.getMethod();boolean guarded=method.equals("POST")&&(path.equals("/api/ccfolia/character-preview")||path.matches("/api/import/session/[0-9]+/sources/text"))||method.equals("PATCH")&&path.matches("/api/import/session/[0-9]+/sources/[0-9]+");
  if(!guarded){chain.doFilter(q,r);return;}if(q.getContentLengthLong()>LIMIT){SecurityConfig.write(r,413,"PAYLOAD_TOO_LARGE","送信データが大きすぎます。");return;}
  byte[] bytes=q.getInputStream().readNBytes(LIMIT+1);if(bytes.length>LIMIT){SecurityConfig.write(r,413,"PAYLOAD_TOO_LARGE","送信データが大きすぎます。");return;}
  chain.doFilter(new HttpServletRequestWrapper(q){public ServletInputStream getInputStream(){var in=new ByteArrayInputStream(bytes);return new ServletInputStream(){public int read(){return in.read();}public int read(byte[] b,int o,int n){return in.read(b,o,n);}public boolean isFinished(){return in.available()==0;}public boolean isReady(){return true;}public void setReadListener(ReadListener l){throw new UnsupportedOperationException();}};}public BufferedReader getReader(){return new BufferedReader(new InputStreamReader(getInputStream(),java.nio.charset.StandardCharsets.UTF_8));}},r);
 }
}
