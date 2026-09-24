package interview.guide.modules.interview.model;

import interview.guide.common.model.AsyncTaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "interview_sessions")
public class InterviewSessionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long candidateId;

  @Column(nullable = false)
  private Long jobId;

  /** 可选关联已评分简历 */
  private Long resumeId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private InterviewSessionStatus status;

  @Column(nullable = false)
  private Integer totalQuestions;

  /** 当前题目下标 0-based */
  @Column(nullable = false)
  private Integer currentIndex;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private AsyncTaskStatus evaluateStatus;

  @Column(length = 500)
  private String evaluateError;

  private Integer overallScore;

  @Column(columnDefinition = "TEXT")
  private String overallFeedback;

  @Column(columnDefinition = "TEXT")
  private String strengthsJson;

  @Column(columnDefinition = "TEXT")
  private String improvementsJson;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime completedAt;

  @PrePersist
  public void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    createdAt = now;
    updatedAt = now;
    if (status == null) {
      status = InterviewSessionStatus.IN_PROGRESS;
    }
    if (currentIndex == null) {
      currentIndex = 0;
    }
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
