package interview.guide.modules.admin.controller;

import interview.guide.common.result.Result;
import interview.guide.modules.admin.model.AdminDtos.JobRequest;
import interview.guide.modules.admin.model.AdminDtos.JobResponse;
import interview.guide.modules.admin.model.AdminDtos.JobStatusRequest;
import interview.guide.modules.admin.service.JobAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/jobs")
@RequiredArgsConstructor
public class JobAdminController {

  private final JobAdminService jobAdminService;

  @GetMapping
  public Result<List<JobResponse>> list() {
    return Result.success(jobAdminService.list());
  }

  @GetMapping("/{id}")
  public Result<JobResponse> get(@PathVariable Long id) {
    return Result.success(jobAdminService.get(id));
  }

  @PostMapping
  public Result<JobResponse> create(@Valid @RequestBody JobRequest request) {
    return Result.success(jobAdminService.create(request));
  }

  @PutMapping("/{id}")
  public Result<JobResponse> update(@PathVariable Long id, @Valid @RequestBody JobRequest request) {
    return Result.success(jobAdminService.update(id, request));
  }

  @PatchMapping("/{id}/status")
  public Result<JobResponse> updateStatus(
      @PathVariable Long id, @Valid @RequestBody JobStatusRequest request) {
    return Result.success(jobAdminService.updateStatus(id, request.status()));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    jobAdminService.delete(id);
    return Result.success();
  }
}
