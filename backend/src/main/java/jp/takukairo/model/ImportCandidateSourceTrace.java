package jp.takukairo.model;
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="import_candidate_source_trace")
public class ImportCandidateSourceTrace {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(name="import_candidate_id") public Long importCandidateId;
 @Column(name="import_source_id") public Long importSourceId;
 @JdbcTypeCode(SqlTypes.JSON) @Column(name="source_location", columnDefinition="jsonb") public String sourceLocation = "{}";
 @Column(name="created_at") public Instant createdAt = Instant.now();
 @Column(name="updated_at") public Instant updatedAt = Instant.now();
}
