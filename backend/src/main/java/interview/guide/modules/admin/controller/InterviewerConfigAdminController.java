package interview.guide.modules.admin.controller;

import interview.guide.common.result.Result;
import interview.guide.modules.admin.model.AdminDtos.InterviewerConfigRequest;
import interview.guide.modules.admin.model.AdminDtos.InterviewerConfigResponse;
import interview.guide.modules.admin.service.InterviewerConfigAdminService;
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
@RequestMapping("/api/admin/interviewer-configs")
@RequiredArgsConstructor
public class InterviewerConfigAdminController {

  private final InterviewerConfigAdminService service;

  @GetMapping
  public Result<List<InterviewerConfigResponse>> list() {
    return Result.success(service.list());
  }

  @GetMapping("/{id}")
  public Result<InterviewerConfigResponse> get(@PathVariable Long id) {
    return Result.success(service.get(id));
  }

  @PostMapping
  public Result<InterviewerConfigResponse> create(
      @Valid @RequestBody InterviewerConfigRequest request) {
    return Result.success(service.create(request));
  }

  @PutMapping("/{id}")
  public Result<InterviewerConfigResponse> update(
      @PathVariable Long id, @Valid @RequestBody InterviewerConfigRequest request) {
    return Result.success(service.update(id, request));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return Result.success();
  }
}
