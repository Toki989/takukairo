package jp.takukairo.security;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import jakarta.servlet.http.*;
import jp.takukairo.common.*;
import jp.takukairo.model.*;
import java.time.*;
import java.util.*;
@RestController @Profile({"dev","test"})
public class DevLoginController {
 private final Store store;private final SecurityContextRepository contexts;private final HttpSessionCsrfTokenRepository csrf;
 public DevLoginController(Store s,SecurityContextRepository c,HttpSessionCsrfTokenRepository t){store=s;contexts=c;csrf=t;}
 @PostMapping("/api/dev/session") @Transactional public Object login(HttpServletRequest q,HttpServletResponse r){var users=store.query(AppUser.class,"where e.providerSubject=:sub","sub","dev-test-sub");AppUser u;if(users.isEmpty()){u=new AppUser();u.authProvider="GOOGLE";u.providerSubject="dev-test-sub";store.save(u);store.flush();}else u=users.getFirst();q.getSession();q.changeSessionId();q.getSession().setAttribute("loginAt",Instant.now().toString());var context=SecurityContextHolder.createEmptyContext();context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(u.id.toString(),null,List.of()));SecurityContextHolder.setContext(context);contexts.saveContext(context,q,r);csrf.saveToken(null,q,r);SecurityEvents.record("AUTH_SUCCESS");return Json.map("authenticated",true);}
}
