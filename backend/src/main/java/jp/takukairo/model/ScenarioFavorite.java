package jp.takukairo.model;
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="scenario_favorite")
public class ScenarioFavorite {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(name="user_id") public Long userId;
 @Column(name="scenario_id") public Long scenarioId;
 @Column(name="created_at") public Instant createdAt = Instant.now();
}
