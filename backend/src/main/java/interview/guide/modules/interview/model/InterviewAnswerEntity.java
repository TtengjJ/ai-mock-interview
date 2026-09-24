package interview.guide.modules.interview.model;

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
@Table(name = "interview_answers")
public class InterviewAnswerEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long sessionId;

  @Column(nullable = false)
  private Integer questionIndex;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String questionText;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private QuestionSource source;

  @Column(columnDefinition = "TEXT")
  private String referenceAnswer;

  @Column(columnDefinition = "TEXT")
  private String answerText;

  @Column(columnDefinition = "TEXT")
  private String followUpQuestion;

  @Column(columnDefinition = "TEXT")
  private String followUpAnswer;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @PrePersist
  public void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  public void preUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
