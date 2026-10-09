package jp.takukairo.security;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.*;
import org.springframework.web.servlet.config.annotation.*;
import jakarta.servlet.http.*;
import java.time.*;
import java.util.*;
import jp.takukairo.common.*;
@Component
public class SetupInterceptor implements HandlerInterceptor,WebMvcConfigurer {
 private final CurrentUser current;
 private final Set<String> allowed=Set.of("/api/session","/api/csrf","/api/logout","/api/self-person","/api/dev/session");
 public SetupInterceptor(CurrentUser c){current=c;}
 public void addInterceptors(InterceptorRegistry r){r.addInterceptor(this).addPathPatterns("/api/**");}
 public boolean preHandle(HttpServletRequest q,HttpServletResponse r,Object h){if(allowed.contains(q.getRequestURI()))return true;var s=q.getSession(false);if(s!=null&&s.getAttribute("loginAt")!=null&&Instant.parse((String)s.getAttribute("loginAt")).plus(Duration.ofDays(30)).isBefore(Instant.now())){s.invalidate();throw new ApiException(401,"SESSION_EXPIRED","ログインしてください。");}if(current.user().selfPersonId==null)throw new ApiException(409,"SELF_PERSON_SETUP_REQUIRED","TRPGで使う名前を設定してください。");return true;}
}
