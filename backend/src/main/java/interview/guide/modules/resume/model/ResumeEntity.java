package interview.guide.modules.resume.model;

import interview.guide.common.model.AsyncTaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "resumes", indexes = {
    @Index(name = "uk_resume_candidate_hash", columnList = "candidateId,contentHash", unique = true)
})
public class ResumeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long candidateId;

  @Column(nullable = false, length = 255)
  private String fileName;

  @Column(nullable = false, length = 64)
  private String contentHash;

  @Column(length = 512)
  private String storageKey;

  @Column(columnDefinition = "TEXT")
  private String rawText;

  private Long fileSize;

  @Column(length = 128)
  private String contentType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private AsyncTaskStatus analyzeStatus;

  @Column(length = 500)
  private String analyzeError;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @PrePersist
  public void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    createdAt = now;
    updatedAt = now;
    if (analyzeStatus == null) {
      analyzeStatus = AsyncTaskStatus.PENDING;
    }
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
