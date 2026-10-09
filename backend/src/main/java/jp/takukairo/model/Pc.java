package jp.takukairo.model;
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="pc")
public class Pc {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(name="user_id") public Long userId;
 @Column(name="person_id") public Long personId;
 @Column(name="name", length=255) public String name;
 @Column(name="character_sheet_url", length=2048) public String characterSheetUrl;
 @Column(name="image_master_key", length=512) public String imageMasterKey;
 @Column(name="image_derivative_key", length=512) public String imageDerivativeKey;
 @Column(name="image_position_x", precision=9, scale=4) public BigDecimal imagePositionX = BigDecimal.ZERO;
 @Column(name="image_position_y", precision=9, scale=4) public BigDecimal imagePositionY = BigDecimal.ZERO;
 @Column(name="image_zoom", precision=9, scale=4) public BigDecimal imageZoom = BigDecimal.ONE;
 @Column(name="created_at") public Instant createdAt = Instant.now();
 @Column(name="updated_at") public Instant updatedAt = Instant.now();
 @Version public long version;
}
