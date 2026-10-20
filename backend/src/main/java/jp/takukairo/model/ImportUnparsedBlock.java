package jp.takukairo.model;
import jakarta.persistence.*;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
/** Temporary Support Data; never a Formal Entity. */
@Entity @Table(name="import_unparsed_block")
public class ImportUnparsedBlock {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(name="import_session_id") public Long importSessionId;
 @Column(name="import_source_id") public Long importSourceId;
 @Column(name="start_offset") public int startOffset;
 @Column(name="end_offset") public int endOffset;
 @Column(name="original_text",columnDefinition="text") public String originalText;
 @Column(columnDefinition="text") public String reason;
 public String state="UNPROCESSED";
 @JdbcTypeCode(SqlTypes.JSON) @Column(name="source_location",columnDefinition="jsonb") public String sourceLocation="{}";
 @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition="jsonb") public String consumption="{}";
 @Version public long version;
 @Column(name="created_at") public Instant createdAt=Instant.now();
 @Column(name="updated_at") public Instant updatedAt=Instant.now();
}
