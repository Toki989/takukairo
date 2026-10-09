package jp.takukairo.image;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.web.multipart.MultipartFile;
import jp.takukairo.common.*;
import jp.takukairo.model.*;
import jp.takukairo.security.*;
import static jp.takukairo.common.Json.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.*;
@Service
public class ImageService {
 private final Store s;private final CurrentUser u;private final LocalStorage storage;private final Cleanup cleanup;private final ImageProcessor processor;
 record Token(long userId,String key,Instant expires){}private final Map<String,Token> urls=Collections.synchronizedMap(new LinkedHashMap<>(100,.75f,true){protected boolean removeEldestEntry(Map.Entry<String,Token> e){return size()>10000;}});
 public ImageService(Store s,CurrentUser u,LocalStorage l,Cleanup c,ImageProcessor p){this.s=s;this.u=u;storage=l;cleanup=c;processor=p;}
 private Object parent(String kind,long id){if(kind.equals("pcs"))return u.owned(Pc.class,id);if(kind.equals("scenarios"))return u.owned(Scenario.class,id);throw ApiException.notFound();}
 private long version(Object p){return p instanceof Pc pc?pc.version:((Scenario)p).version;}
 private String derivative(Object p){return p instanceof Pc pc?pc.imageDerivativeKey:((Scenario)p).imageDerivativeKey;}
 private String master(Object p){return p instanceof Pc pc?pc.imageMasterKey:((Scenario)p).imageMasterKey;}
 private void set(Object p,String master,String derivative){if(p instanceof Pc pc){pc.imageMasterKey=master;pc.imageDerivativeKey=derivative;pc.updatedAt=Instant.now();}else{var sc=(Scenario)p;sc.imageMasterKey=master;sc.imageDerivativeKey=derivative;sc.updatedAt=Instant.now();if(master!=null)sc.externalImageUrl=null;}}
 private Object response(Object p){return map("id",p instanceof Pc pc?pc.id:((Scenario)p).id,"version",version(p),"image",derivative(p)==null?null:p instanceof Pc pc?map("present",true,"positionX",pc.imagePositionX,"positionY",pc.imagePositionY,"zoom",pc.imageZoom):map("present",true));}
 private BigDecimal decimal(Object v,String path){try{BigDecimal d=new BigDecimal(v.toString()).setScale(4,java.math.RoundingMode.HALF_UP);if(d.precision()>9)throw new Exception();return d;}catch(Exception e){throw ApiException.invalid(path,"INVALID_NUMBER","数値を確認してください。");}}
 private void transform(Pc pc,Map<String,Object>b){pc.imagePositionX=decimal(b.get("positionX"),"/positionX");pc.imagePositionY=decimal(b.get("positionY"),"/positionY");pc.imageZoom=decimal(b.get("zoom"),"/zoom");if(pc.imageZoom.signum()<=0)throw ApiException.invalid("/zoom","INVALID_NUMBER","倍率は0より大きくしてください。");}
 @Transactional public Object put(String kind,long id,long expected,MultipartFile f,Map<String,Object>b){Object p=parent(kind,id);Store.version(version(p),expected);ImageProcessor.Processed result;try{result=processor.process(f.getBytes());}catch(java.io.IOException e){throw ApiException.invalid("/file","INVALID_IMAGE","画像を読み取れませんでした。");}String mk=null,dk=null;try{mk=storage.save(result.master(),"png");dk=storage.save(result.derivative(),"webp");}catch(Exception e){cleanup.compensate(mk);cleanup.compensate(dk);throw new ApiException(500,"INTERNAL_ERROR","画像を保存できませんでした。");}final String newMaster=mk,newDerivative=dk;TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){public void afterCompletion(int status){if(status!=STATUS_COMMITTED){cleanup.compensate(newMaster);cleanup.compensate(newDerivative);}}});s.entityManager().refresh(p,jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);Store.version(version(p),expected);cleanup.schedule(master(p));cleanup.schedule(derivative(p));set(p,mk,dk);if(p instanceof Pc pc)transform(pc,b);s.flush();return response(p);}
 @Transactional public Object remove(String kind,long id,long expected){var p=parent(kind,id);Store.version(version(p),expected);cleanup.schedule(master(p));cleanup.schedule(derivative(p));set(p,null,null);if(p instanceof Pc pc){pc.imagePositionX=BigDecimal.ZERO;pc.imagePositionY=BigDecimal.ZERO;pc.imageZoom=BigDecimal.ONE;}s.flush();return response(p);}
 @Transactional public Object transform(long id,Map<String,Object>b){var p=u.owned(Pc.class,id);Store.version(p.version,requiredLong(b,"expectedVersion"));transform(p,b);p.updatedAt=Instant.now();s.flush();return response(p);}
 @Transactional(readOnly=true) public Object url(String kind,long id){var p=parent(kind,id);String key=derivative(p);if(key==null)throw ApiException.notFound();String token=UUID.randomUUID().toString();Instant expiry=Instant.now().plusSeconds(300);urls.put(token,new Token(u.id(),key,expiry));return map("url","/api/dev-images/"+token,"expiresAt",expiry);}
 @Transactional(readOnly=true) public byte[] read(String token){var v=urls.get(token);if(v==null||v.userId!=u.id()||v.expires.isBefore(Instant.now()))throw new ApiException(403,"FORBIDDEN","画像URLの期限が切れています。");try{return storage.read(v.key);}catch(Exception e){throw ApiException.notFound();}}
}
