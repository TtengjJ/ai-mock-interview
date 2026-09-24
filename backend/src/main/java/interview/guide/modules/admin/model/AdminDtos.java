package interview.guide.modules.admin.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public final class AdminDtos {

  private AdminDtos() {}

  public record PageResponse<T>(List<T> items, long total, int page, int size) {}

  // ---- Job ----
  public record JobRequest(
      @NotBlank(message = "岗位名称不能为空") String jobName,
      String jobDesc,
      Long interviewerConfigId,
      String prompt,
      Integer status
  ) {}

  public record JobStatusRequest(@NotNull(message = "状态不能为空") Integer status) {}

  public record JobResponse(
      Long id,
      String jobName,
      String jobDesc,
      Integer status,
      Long interviewerConfigId,
      String prompt,
      LocalDateTime createdAt,
      LocalDateTime updatedAt
  ) {}

  // ---- Question ----
  public record QuestionRequest(
      @NotNull(message = "岗位ID不能为空") Long jobId,
      @NotBlank(message = "题目不能为空") String question,
      String referenceAnswer,
      Integer isOn,
      Integer sortOrder
  ) {}

  public record QuestionOnRequest(@NotNull(message = "启停状态不能为空") Integer isOn) {}

  public record QuestionResponse(
      Long id,
      Long jobId,
      String question,
      String referenceAnswer,
      Integer isOn,
      Integer sortOrder,
      LocalDateTime createdAt,
      LocalDateTime updatedAt
  ) {}

  // ---- Candidate ----
  public record CandidateRequest(
      @NotBlank(message = "姓名不能为空") String realName,
      @NotBlank(message = "手机号不能为空") String mobile,
      String email,
      Integer sex,
      String password,
      Long jobId,
      String remark
  ) {}

  public record CandidateResponse(
      Long id,
      String realName,
      String mobile,
      String email,
      Integer sex,
      Long jobId,
      String remark,
      LocalDateTime createdAt,
      LocalDateTime updatedAt
  ) {}

  // ---- InterviewerConfig ----
  public record InterviewerConfigRequest(
      @NotBlank(message = "AI名称不能为空") String aiName,
      String systemPrompt
  ) {}

  public record InterviewerConfigResponse(
      Long id,
      String aiName,
      String systemPrompt,
      LocalDateTime createdAt,
      LocalDateTime updatedAt
  ) {}
}
