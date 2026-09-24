package interview.guide.modules.interview.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public final class InterviewDtos {

  private InterviewDtos() {}

  public record CreateSessionRequest(
      @NotNull(message = "岗位ID不能为空") Long jobId,
      Long resumeId
  ) {}

  public record SubmitAnswerRequest(
      @NotBlank(message = "作答内容不能为空") String answerText
  ) {}

  public record JobOptionResponse(Long id, String jobName, String jobDesc) {}

  public record QuestionView(
      Integer questionIndex,
      String questionText,
      String source,
      String answerText,
      String followUpQuestion,
      String followUpAnswer,
      boolean answered,
      boolean followUpGenerated,
      boolean followUpAnswered
  ) {}

  public record SessionSummaryResponse(
      Long id,
      Long jobId,
      String jobName,
      Long resumeId,
      String status,
      String evaluateStatus,
      Integer overallScore,
      Integer totalQuestions,
      Integer currentIndex,
      LocalDateTime createdAt,
      LocalDateTime completedAt
  ) {}

  public record SessionDetailResponse(
      Long id,
      Long candidateId,
      String candidateName,
      Long jobId,
      String jobName,
      Long resumeId,
      String status,
      String evaluateStatus,
      String evaluateError,
      Integer overallScore,
      String overallFeedback,
      List<String> strengths,
      List<String> improvements,
      Integer totalQuestions,
      Integer currentIndex,
      List<QuestionView> questions,
      LocalDateTime createdAt,
      LocalDateTime completedAt
  ) {}

  public record PageResponse<T>(List<T> items, long total, int page, int size) {}
}
