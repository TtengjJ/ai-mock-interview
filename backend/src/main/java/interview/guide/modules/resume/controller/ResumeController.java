package interview.guide.modules.resume.controller;

import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import interview.guide.common.result.Result;
import interview.guide.modules.auth.service.AuthContextHolder;
import interview.guide.modules.resume.model.ResumeDtos.ResumeDetailDTO;
import interview.guide.modules.resume.model.ResumeDtos.ResumeListItemDTO;
import interview.guide.modules.resume.model.ResumeDtos.UploadResponse;
import interview.guide.modules.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

  private final ResumeService resumeService;

  @PostMapping("/upload")
  public Result<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
    return Result.success(resumeService.upload(requireCandidateId(), file));
  }

  @GetMapping
  public Result<List<ResumeListItemDTO>> list() {
    return Result.success(resumeService.list(requireCandidateId()));
  }

  @GetMapping("/{id}/detail")
  public Result<ResumeDetailDTO> detail(@PathVariable Long id) {
    return Result.success(resumeService.detail(requireCandidateId(), id));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    resumeService.delete(requireCandidateId(), id);
    return Result.success();
  }

  private Long requireCandidateId() {
    Long id = AuthContextHolder.currentCandidateId();
    if (id == null) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
    return id;
  }
}
