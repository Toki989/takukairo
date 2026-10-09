package jp.takukairo.security;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.web.csrf.CsrfToken;
import jp.takukairo.common.*;
import jp.takukairo.model.*;
import static jp.takukairo.common.Json.*;
import java.util.*;
@RestController @RequestMapping("/api")
public class AuthController {
 private final CurrentUser current;private final Store store;
 public AuthController(CurrentUser c,Store s){current=c;store=s;}
 @GetMapping("/csrf") Object csrf(CsrfToken token){return map("token",token.getToken(),"headerName",token.getHeaderName(),"parameterName",token.getParameterName());}
 @GetMapping("/session") @Transactional(readOnly=true) public Object session(){try{var u=current.user();var self=u.selfPersonId==null?null:store.find(Person.class,u.selfPersonId);return map("authenticated",true,"user",map("id",u.id,"selfPerson",self==null?null:map("id",self.id,"displayName",self.displayName),"externalImageConsentGiven",u.externalImageConsentAt!=null));}catch(ApiException e){if(e.status==401)return map("authenticated",false,"user",null);throw e;}}
 @PostMapping("/self-person") @Transactional Object setup(@RequestBody Map<String,Object> body){var u=store.lock(AppUser.class,current.id());if(u.selfPersonId!=null)throw new ApiException(409,"CONFLICT","名前は設定済みです。");String name=text(body.get("displayName"));if(name==null||name.isBlank()||name.length()>255)throw ApiException.invalid("/displayName","REQUIRED","TRPGで使う名前を入力してください。");var p=new Person();p.userId=u.id;p.displayName=name.trim();store.save(p);store.flush();u.selfPersonId=p.id;return map("id",p.id,"displayName",p.displayName,"version",p.version);}
}
