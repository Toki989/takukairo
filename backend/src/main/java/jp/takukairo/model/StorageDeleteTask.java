package jp.takukairo.model;
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="storage_delete_task")
public class StorageDeleteTask {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(name="object_key", length=512) public String objectKey;
 @Column(name="attempt_count") public Integer attemptCount;
 @Column(name="next_attempt_at") public Instant nextAttemptAt = Instant.now();
 @Column(name="last_error_code", length=255) public String lastErrorCode;
 @Column(name="status", length=32) public String status;
 @Column(name="created_at") public Instant createdAt = Instant.now();
}
