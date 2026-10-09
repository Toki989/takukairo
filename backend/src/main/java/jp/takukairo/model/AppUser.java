package jp.takukairo.model;
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity @Table(name="app_user")
public class AppUser {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(name="auth_provider", length=32) public String authProvider;
 @Column(name="provider_subject", length=255) public String providerSubject;
 @Column(name="self_person_id") public Long selfPersonId;
 @Column(name="external_image_consent_at") public Instant externalImageConsentAt;
 @Column(name="created_at") public Instant createdAt = Instant.now();
 @Column(name="updated_at") public Instant updatedAt = Instant.now();
 @Version public long version;
}
