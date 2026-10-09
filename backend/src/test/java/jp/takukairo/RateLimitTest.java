package jp.takukairo;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.*;
import jp.takukairo.security.RateLimitFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import static org.junit.jupiter.api.Assertions.*;
class RateLimitTest {
 @AfterEach void clear(){SecurityContextHolder.clearContext();}
 @Test void enabledAuthIpLimitAndNoBucketDetails()throws Exception{var f=new RateLimitFilter(true);for(int i=0;i<21;i++){var q=new MockHttpServletRequest("POST","/api/dev/session");var r=new MockHttpServletResponse();f.doFilter(q,r,(request,response)->{});if(i<20)assertEquals(200,r.getStatus());else{assertEquals(429,r.getStatus());assertEquals("60",r.getHeader("Retry-After"));assertTrue(r.getContentAsString().contains("RATE_LIMITED"));assertFalse(r.getContentAsString().contains("remaining"));}}}
 @Test void enabledBoothUserLimit()throws Exception{SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated("1",null,java.util.List.of()));var f=new RateLimitFilter(true);for(int i=0;i<11;i++){var q=new MockHttpServletRequest("POST","/api/booth/preview");var r=new MockHttpServletResponse();f.doFilter(q,r,(request,response)->{});assertEquals(i==10?429:200,r.getStatus());}}
 @Test void onlyTestOverrideCanDisable()throws Exception{var f=new RateLimitFilter(false);for(int i=0;i<30;i++){var r=new MockHttpServletResponse();f.doFilter(new MockHttpServletRequest("POST","/api/dev/session"),r,(request,response)->{});assertEquals(200,r.getStatus());}}
}
