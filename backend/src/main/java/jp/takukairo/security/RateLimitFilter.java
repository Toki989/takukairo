package jp.takukairo.security;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import io.github.bucket4j.*;
import java.time.*;
import java.util.*;
@Component @org.springframework.core.annotation.Order(0)
public class RateLimitFilter extends OncePerRequestFilter {
 private final boolean enabled;private final Map<String,Bucket> buckets=Collections.synchronizedMap(new LinkedHashMap<>(256,.75f,true){protected boolean removeEldestEntry(Map.Entry<String,Bucket> e){return size()>10000;}});
 public RateLimitFilter(@Value("${app.rate-limit:true}")boolean enabled){this.enabled=enabled;}
 private boolean consume(String key,int limit,long seconds){Bucket b; synchronized(buckets){b=buckets.computeIfAbsent(key,k->Bucket.builder().addLimit(Bandwidth.builder().capacity(limit).refillGreedy(limit,Duration.ofSeconds(seconds)).build()).build());}return b.tryConsume(1);}
 protected void doFilterInternal(HttpServletRequest q,HttpServletResponse r,FilterChain c)throws ServletException,java.io.IOException{if(!enabled||q.getMethod().equals("OPTIONS")||!q.getRequestURI().startsWith("/api")){c.doFilter(q,r);return;}String path=q.getRequestURI(),ip=q.getRemoteAddr();var a=SecurityContextHolder.getContext().getAuthentication();String uid=a!=null&&a.isAuthenticated()&&!(a instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)?a.getName():null;String group="other";int userLimit=120,ipLimit=300;long seconds=60;int hourUser=0,hourIp=0;if(path.equals("/api/dev/session")||path.equals("/api/auth/google")){group="auth";ipLimit=20;hourIp=60;}else if(path.equals("/api/booth/preview")){group="booth";userLimit=10;ipLimit=30;seconds=600;hourUser=30;hourIp=100;}else if(path.contains("/sources/files")||path.endsWith("/file")){group="importUpload";userLimit=30;ipLimit=300;seconds=600;hourUser=60;}else if(path.contains("/import/")&&(path.endsWith("/analyze")||path.endsWith("/register")||path.contains("preview")||path.endsWith("/split")||path.endsWith("/merge"))){group="importHeavy";userLimit=10;ipLimit=300;seconds=600;hourUser=30;}else if(path.endsWith("/image")&&q.getMethod().equals("PUT")){group="upload";userLimit=20;ipLimit=60;seconds=600;hourUser=60;hourIp=200;}else if(q.getParameter("search")!=null){group="search";userLimit=60;ipLimit=180;}boolean allowed=consume(group+":ip:"+ip,ipLimit,seconds)&&(uid==null||consume(group+":u:"+uid,userLimit,seconds))&&(hourIp==0||consume(group+":iph:"+ip,hourIp,3600))&&(hourUser==0||uid==null||consume(group+":uh:"+uid,hourUser,3600));if(!allowed){SecurityEvents.record("RATE_LIMIT");r.setHeader("Retry-After","60");SecurityConfig.write(r,429,"RATE_LIMITED","少し待ってから、もう一度お試しください。");return;}c.doFilter(q,r);}
}
