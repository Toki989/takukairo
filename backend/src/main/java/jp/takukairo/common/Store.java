package jp.takukairo.common;
import jakarta.persistence.*;
import java.util.*;
import org.springframework.stereotype.Repository;
@Repository
public class Store {
 @PersistenceContext public EntityManager em;
 public EntityManager entityManager(){return em;}
 public <T> T find(Class<T> type,long id){T v=em.find(type,id);if(v==null)throw ApiException.notFound();return v;}
 public <T> T lock(Class<T> type,long id){T v=em.find(type,id,LockModeType.PESSIMISTIC_WRITE);if(v==null)throw ApiException.notFound();return v;}
 public <T> List<T> query(Class<T> type,String where,Object... args){var q=em.createQuery("from "+type.getSimpleName()+" e "+where,type);for(int i=0;i<args.length;i+=2)q.setParameter((String)args[i],args[i+1]);return q.getResultList();}
 public <T> T save(T v){em.persist(v);return v;}
 public void remove(Object v){em.remove(v);}
 public void flush(){em.flush();}
 public long count(String jpql,Object...args){var q=em.createQuery(jpql,Long.class);for(int i=0;i<args.length;i+=2)q.setParameter((String)args[i],args[i+1]);return q.getSingleResult();}
 public static void version(long actual,long expected){if(actual!=expected)throw ApiException.conflict();}
}
