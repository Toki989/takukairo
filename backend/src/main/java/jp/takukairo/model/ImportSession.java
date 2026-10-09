package jp.takukairo.model;
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="import_session")
public class ImportSession {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(name="user_id") public Long userId;
 @Column(name="status", length=32) public String status;
 @Column(name="current_step", length=32) public String currentStep;
 @Column(name="saved_at") public Instant savedAt = Instant.now();
 @Column(name="expires_at") public Instant expiresAt = Instant.now();
 @Column(name="completed_at") public Instant completedAt;
 @Column(name="delete_after_at") public Instant deleteAfterAt;
 @Column(name="created_at") public Instant createdAt = Instant.now();
 @Column(name="updated_at") public Instant updatedAt = Instant.now();
 @Version public long version;
}
