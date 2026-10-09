package jp.takukairo.model;
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="participation")
public class Participation {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(name="table_id") public Long tableId;
 @Column(name="person_id") public Long personId;
 @Column(name="pc_id") public Long pcId;
 @Column(name="role", length=16) public String role;
 @Column(name="display_order") public Integer displayOrder;
 @Column(name="ho", columnDefinition="text") public String ho;
 @Column(name="display_quote", columnDefinition="text") public String displayQuote;
 @Column(name="spotlight_type", length=16) public String spotlightType;
 @Column(name="created_at") public Instant createdAt = Instant.now();
 @Column(name="updated_at") public Instant updatedAt = Instant.now();
 @Version public long version;
}
