package interview.guide.modules.admin.controller;

import interview.guide.common.constant.CommonConstants;
import interview.guide.common.result.Result;
import interview.guide.modules.interview.model.InterviewDtos.PageResponse;
import interview.guide.modules.interview.model.InterviewDtos.SessionDetailResponse;
import interview.guide.modules.interview.service.InterviewSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/interview-records")
@RequiredArgsConstructor
public class InterviewRecordAdminController {

  private final InterviewSessionService interviewSessionService;

  @GetMapping
  public Result<PageResponse<SessionDetailResponse>> list(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size) {
    int safePage = Math.max(page, CommonConstants.Pagination.DEFAULT_PAGE);
    int safeSize = Math.min(Math.max(size, 1), CommonConstants.Pagination.MAX_SIZE);
    return Result.success(interviewSessionService.adminList(safePage, safeSize));
  }

  @GetMapping("/{id}")
  public Result<SessionDetailResponse> detail(@PathVariable Long id) {
    return Result.success(interviewSessionService.adminGet(id));
  }
}
