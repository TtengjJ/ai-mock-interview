package interview.guide.modules.resume.model;

import interview.guide.common.model.AsyncTaskStatus;

import java.time.LocalDateTime;
import java.util.List;

public final class ResumeDtos {

  private ResumeDtos() {}

  public record ResumeListItemDTO(
      Long id,
      String fileName,
      AsyncTaskStatus analyzeStatus,
      Integer overallScore,
      LocalDateTime createdAt
  ) {}

  public record ResumeAnalysisDTO(
      Integer overallScore,
      String summary,
      List<String> strengths,
      List<String> suggestions,
      LocalDateTime analyzedAt
  ) {}

  public record ResumeDetailDTO(
      Long id,
      String fileName,
      AsyncTaskStatus analyzeStatus,
      String analyzeError,
      LocalDateTime createdAt,
      ResumeAnalysisDTO analysis
  ) {}

  public record UploadResponse(
      Long resumeId,
      String fileName,
      AsyncTaskStatus analyzeStatus,
      boolean duplicate
  ) {}

  public record AnalyzeResult(
      Integer overallScore,
      String summary,
      List<String> strengths,
      List<String> suggestions
  ) {}
}
