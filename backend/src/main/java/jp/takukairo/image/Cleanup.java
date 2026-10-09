package jp.takukairo.image;
import jp.takukairo.common.*;
import jp.takukairo.model.*;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
@Service
public class Cleanup {
 private final Store s;private final LocalStorage storage;
 public Cleanup(Store s,LocalStorage l){this.s=s;storage=l;}
 public void schedule(String key){if(key==null)return;var t=new StorageDeleteTask();t.objectKey=key;t.attemptCount=0;t.nextAttemptAt=Instant.now();t.status="PENDING";s.save(t);}
 @Transactional(propagation=org.springframework.transaction.annotation.Propagation.REQUIRES_NEW) public void compensate(String key){try{storage.delete(key);}catch(Exception e){schedule(key);}}
 @Scheduled(fixedDelay=60000) @Transactional public void run(){for(var t:s.query(StorageDeleteTask.class,"where e.status='PENDING' and e.nextAttemptAt<=:now order by e.id","now",Instant.now())){try{storage.delete(t.objectKey);s.remove(t);}catch(Exception e){t.attemptCount++;t.lastErrorCode="STORAGE_DELETE_FAILED";if(t.createdAt.plus(Duration.ofDays(30)).isBefore(Instant.now()))t.status="DEAD";else t.nextAttemptAt=Instant.now().plusSeconds(Math.min(86400,60L<<(Math.min(t.attemptCount-1,11))));org.slf4j.LoggerFactory.getLogger(Cleanup.class).warn("Storage deletion retry failed");}}}
}
