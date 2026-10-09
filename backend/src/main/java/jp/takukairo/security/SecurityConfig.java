package jp.takukairo.security;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.*;
import org.springframework.security.web.context.*;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.*;
import java.util.*;
import jp.takukairo.common.*;
@Configuration
public class SecurityConfig {
 @Bean org.springframework.security.core.userdetails.UserDetailsService noPasswordLogin(){return name->{throw new org.springframework.security.core.userdetails.UsernameNotFoundException("Password login is unavailable");};}
 @Bean HttpSessionCsrfTokenRepository csrfRepository(){return new HttpSessionCsrfTokenRepository();}
 @Bean SecurityContextRepository contextRepository(){return new HttpSessionSecurityContextRepository();}
 @Bean SecurityFilterChain security(HttpSecurity http,HttpSessionCsrfTokenRepository csrf,SecurityContextRepository context) throws Exception {
  var handler=new CsrfTokenRequestAttributeHandler();
  return http.cors(c->{}).csrf(c->c.csrfTokenRepository(csrf).csrfTokenRequestHandler(handler))
   .securityContext(c->c.securityContextRepository(context))
   .authorizeHttpRequests(a->a.requestMatchers("/api/csrf","/api/session","/api/dev/session").permitAll().anyRequest().authenticated())
   .exceptionHandling(e->e.authenticationEntryPoint((q,r,x)->write(r,401,"SESSION_EXPIRED","ログインしてください。"))
    .accessDeniedHandler((q,r,x)->write(r,403,"FORBIDDEN","操作を許可できません。")))
   .logout(l->l.logoutUrl("/api/logout").logoutSuccessHandler((q,r,a)->r.setStatus(204)))
   .build();
 }
 public static void write(jakarta.servlet.http.HttpServletResponse r,int status,String code,String message)throws java.io.IOException{if(status==401)SecurityEvents.record("AUTH_FAILURE");r.setStatus(status);r.setContentType("application/json;charset=UTF-8");r.getWriter().write(Json.write(Errors.body(new ApiException(status,code,message))));}
 @Bean CorsConfigurationSource cors(){var c=new CorsConfiguration();c.setAllowedOrigins(List.of("http://localhost:5173"));c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));c.setAllowedHeaders(List.of("Content-Type","X-CSRF-TOKEN"));c.setAllowCredentials(true);var s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s;}
 @Bean DefaultCookieSerializer cookies(Environment env){boolean local=env.matchesProfiles("dev","test");var c=new DefaultCookieSerializer();c.setCookieName(local?"TAKUKAIRO_SESSION_DEV":"__Host-TAKUKAIRO_SESSION");c.setUseSecureCookie(!local);c.setUseHttpOnlyCookie(true);c.setSameSite("Lax");c.setCookiePath("/");c.setCookieMaxAge(30*86400);return c;}
}
