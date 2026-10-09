package jp.takukairo.model;
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="import_preview")
public class ImportPreview {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(name="import_session_id") public Long importSessionId;
 @Column(name="revision", length=255) public String revision;
 @Column(name="operation_kind", length=255) public String operationKind;
 @Column(name="session_version") public Long sessionVersion;
 @JdbcTypeCode(SqlTypes.JSON) @Column(name="payload", columnDefinition="jsonb") public String payload = "{}";
 @JdbcTypeCode(SqlTypes.JSON) @Column(name="response", columnDefinition="jsonb") public String response = "{}";
 @Column(name="created_at") public Instant createdAt = Instant.now();
}
