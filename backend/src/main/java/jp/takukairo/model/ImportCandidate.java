package jp.takukairo.model;
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="import_candidate")
public class ImportCandidate {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(name="import_session_id") public Long importSessionId;
 @JdbcTypeCode(SqlTypes.JSON) @Column(name="candidate_data", columnDefinition="jsonb") public String candidateData = "{}";
 @Column(name="status", length=32) public String status;
 @Column(name="confirmation_status", length=32) public String confirmationStatus;
 @Column(name="registration_target") public Boolean registrationTarget;
 @Column(name="registration_status", length=32) public String registrationStatus;
 @JdbcTypeCode(SqlTypes.JSON) @Column(name="registration_result", columnDefinition="jsonb") public String registrationResult = "{}";
 @JdbcTypeCode(SqlTypes.JSON) @Column(name="resolution_refs", columnDefinition="jsonb") public String resolutionRefs = "{}";
 @Column(name="created_at") public Instant createdAt = Instant.now();
 @Column(name="updated_at") public Instant updatedAt = Instant.now();
 @Version public long version;
}
