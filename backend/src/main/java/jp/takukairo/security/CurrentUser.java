package jp.takukairo.security;
import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContextHolder;
import jp.takukairo.common.*;
import jp.takukairo.model.*;
@Component
public class CurrentUser {
 private final Store store;
 public CurrentUser(Store s){store=s;}
 public long id(){var a=SecurityContextHolder.getContext().getAuthentication();if(a==null||!a.isAuthenticated()||a.getPrincipal().equals("anonymousUser"))throw new ApiException(401,"SESSION_EXPIRED","ログインしてください。");return Long.parseLong(a.getName());}
 public AppUser user(){return store.find(AppUser.class,id());}
 public <T> T owned(Class<T> c,long id){T e=store.find(c,id);try{if(!e.getClass().getField("userId").get(e).equals(id())){SecurityEvents.record("OWNERSHIP_DENIED");throw ApiException.notFound();}return e;}catch(ReflectiveOperationException x){throw new IllegalStateException(x);}}
}
