package jp.takukairo.model;
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="scenario")
public class Scenario {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(name="user_id") public Long userId;
 @Column(name="name", length=255) public String name;
 @Column(name="author_name", length=255) public String authorName;
 @Column(name="source_url", length=2048) public String sourceUrl;
 @Column(name="game_system", length=255) public String gameSystem;
 @Column(name="external_image_url", length=2048) public String externalImageUrl;
 @Column(name="image_master_key", length=512) public String imageMasterKey;
 @Column(name="image_derivative_key", length=512) public String imageDerivativeKey;
 @Column(name="created_at") public Instant createdAt = Instant.now();
 @Column(name="updated_at") public Instant updatedAt = Instant.now();
 @Version public long version;
}
