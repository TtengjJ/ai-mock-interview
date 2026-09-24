package interview.guide.modules.interview.controller;

import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import interview.guide.common.result.Result;
import interview.guide.modules.auth.service.AuthContextHolder;
import interview.guide.modules.interview.model.InterviewDtos.CreateSessionRequest;
import interview.guide.modules.interview.model.InterviewDtos.JobOptionResponse;
import interview.guide.modules.interview.model.InterviewDtos.SessionDetailResponse;
import interview.guide.modules.interview.model.InterviewDtos.SessionSummaryResponse;
import interview.guide.modules.interview.model.InterviewDtos.SubmitAnswerRequest;
import interview.guide.modules.interview.service.InterviewSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

  private final InterviewSessionService interviewSessionService;

  @GetMapping("/jobs")
  public Result<List<JobOptionResponse>> listJobs() {
    return Result.success(interviewSessionService.listOpenJobs());
  }

  @PostMapping("/sessions")
  public Result<SessionDetailResponse> create(@Valid @RequestBody CreateSessionRequest request) {
    return Result.success(interviewSessionService.createSession(requireCandidateId(), request));
  }

  @GetMapping("/sessions")
  public Result<List<SessionSummaryResponse>> list() {
    return Result.success(interviewSessionService.listMySessions(requireCandidateId()));
  }

  @GetMapping("/sessions/{id}")
  public Result<SessionDetailResponse> detail(@PathVariable Long id) {
    return Result.success(interviewSessionService.getMySession(requireCandidateId(), id));
  }

  @PostMapping("/sessions/{id}/answers")
  public Result<SessionDetailResponse> submitAnswer(
      @PathVariable Long id, @Valid @RequestBody SubmitAnswerRequest request) {
    return Result.success(
        interviewSessionService.submitAnswer(requireCandidateId(), id, request.answerText()));
  }

  @PostMapping("/sessions/{id}/follow-up")
  public Result<SessionDetailResponse> followUp(@PathVariable Long id) {
    return Result.success(interviewSessionService.generateFollowUp(requireCandidateId(), id));
  }

  @PostMapping("/sessions/{id}/follow-up/answer")
  public Result<SessionDetailResponse> followUpAnswer(
      @PathVariable Long id, @Valid @RequestBody SubmitAnswerRequest request) {
    return Result.success(
        interviewSessionService.submitFollowUpAnswer(requireCandidateId(), id, request.answerText()));
  }

  @PostMapping("/sessions/{id}/next")
  public Result<SessionDetailResponse> next(@PathVariable Long id) {
    return Result.success(interviewSessionService.nextQuestion(requireCandidateId(), id));
  }

  @PostMapping("/sessions/{id}/complete")
  public Result<SessionDetailResponse> complete(@PathVariable Long id) {
    return Result.success(interviewSessionService.complete(requireCandidateId(), id));
  }

  private Long requireCandidateId() {
    Long id = AuthContextHolder.currentCandidateId();
    if (id == null) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
    return id;
  }
}
