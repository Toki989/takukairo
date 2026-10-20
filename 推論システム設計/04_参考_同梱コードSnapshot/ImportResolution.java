package jp.takukairo.model;
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="import_resolution")
public class ImportResolution {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(name="import_session_id") public Long importSessionId;
 @Column(name="entity_type", length=32) public String entityType;
 @Column(name="decision", length=32) public String decision;
 @Column(name="existing_entity_id") public Long existingEntityId;
 @JdbcTypeCode(SqlTypes.JSON) @Column(name="draft_data", columnDefinition="jsonb") public String draftData = "{}";
 @Column(name="created_entity_id") public Long createdEntityId;
 @Column(name="created_at") public Instant createdAt = Instant.now();
 @Column(name="updated_at") public Instant updatedAt = Instant.now();
 @Version public long version;
}
