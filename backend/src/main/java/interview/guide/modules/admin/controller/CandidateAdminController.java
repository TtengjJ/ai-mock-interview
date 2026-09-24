package interview.guide.modules.admin.controller;

import interview.guide.common.result.Result;
import interview.guide.modules.admin.model.AdminDtos.CandidateRequest;
import interview.guide.modules.admin.model.AdminDtos.CandidateResponse;
import interview.guide.modules.admin.service.CandidateAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/candidates")
@RequiredArgsConstructor
public class CandidateAdminController {

  private final CandidateAdminService candidateAdminService;

  @GetMapping
  public Result<List<CandidateResponse>> list() {
    return Result.success(candidateAdminService.list());
  }

  @GetMapping("/{id}")
  public Result<CandidateResponse> get(@PathVariable Long id) {
    return Result.success(candidateAdminService.get(id));
  }

  @PostMapping
  public Result<CandidateResponse> create(@Valid @RequestBody CandidateRequest request) {
    return Result.success(candidateAdminService.create(request));
  }

  @PutMapping("/{id}")
  public Result<CandidateResponse> update(
      @PathVariable Long id, @Valid @RequestBody CandidateRequest request) {
    return Result.success(candidateAdminService.update(id, request));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    candidateAdminService.delete(id);
    return Result.success();
  }
}
