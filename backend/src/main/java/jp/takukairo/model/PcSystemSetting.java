package jp.takukairo.model;
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="pc_system_setting")
public class PcSystemSetting {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(name="pc_id") public Long pcId;
 @Column(name="game_system_name", length=255) public String gameSystemName;
 @Column(name="canonical_system_key", length=128) public String canonicalSystemKey;
 @Column(name="profile_key", length=128) public String profileKey;
 @JdbcTypeCode(SqlTypes.JSON) @Column(name="profile_values", columnDefinition="jsonb") public String profileValues = "{}";
 @Column(name="character_sheet_url", length=2048) public String characterSheetUrl;
 @Column(name="display_order") public Integer displayOrder;
 @Column(name="created_at") public Instant createdAt = Instant.now();
 @Column(name="updated_at") public Instant updatedAt = Instant.now();
}
