package jp.takukairo.model;
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="end_pc_state")
public class EndPcState {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(name="participation_id") public Long participationId;
 @Column(name="profile_key", length=128) public String profileKey;
 @JdbcTypeCode(SqlTypes.JSON) @Column(name="status_values", columnDefinition="jsonb") public String statusValues = "{}";
 @Column(name="growth", columnDefinition="text") public String growth;
 @Column(name="outcome", length=16) public String outcome;
 @Column(name="aftereffects", columnDefinition="text") public String aftereffects;
 @Column(name="created_at") public Instant createdAt = Instant.now();
 @Column(name="updated_at") public Instant updatedAt = Instant.now();
 @Version public long version;
}
