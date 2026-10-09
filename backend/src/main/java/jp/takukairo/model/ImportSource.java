package jp.takukairo.model;
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="import_source")
public class ImportSource {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(name="import_session_id") public Long importSessionId;
 @Column(name="source_type", length=32) public String sourceType;
 @Column(name="original_file_name", length=255) public String originalFileName;
 @Column(name="encoding", length=255) public String encoding;
 @Column(name="temporary_storage_key", length=512) public String temporaryStorageKey;
 @Column(name="raw_text", columnDefinition="text") public String rawText;
 @Column(name="source_order") public Integer sourceOrder;
 @Column(name="raw_byte_length") public Long rawByteLength;
 @Column(name="created_at") public Instant createdAt = Instant.now();
 @Column(name="updated_at") public Instant updatedAt = Instant.now();
 @Version public long version;
}
