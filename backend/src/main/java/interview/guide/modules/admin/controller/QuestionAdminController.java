package interview.guide.modules.admin.controller;

import interview.guide.common.result.Result;
import interview.guide.modules.admin.model.AdminDtos.QuestionOnRequest;
import interview.guide.modules.admin.model.AdminDtos.QuestionRequest;
import interview.guide.modules.admin.model.AdminDtos.QuestionResponse;
import interview.guide.modules.admin.service.QuestionAdminService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/questions")
@RequiredArgsConstructor
public class QuestionAdminController {

  private final QuestionAdminService questionAdminService;

  @GetMapping
  public Result<List<QuestionResponse>> list(@RequestParam(required = false) Long jobId) {
    return Result.success(questionAdminService.list(jobId));
  }

  @GetMapping("/{id}")
  public Result<QuestionResponse> get(@PathVariable Long id) {
    return Result.success(questionAdminService.get(id));
  }

  @PostMapping
  public Result<QuestionResponse> create(@Valid @RequestBody QuestionRequest request) {
    return Result.success(questionAdminService.create(request));
  }

  @PutMapping("/{id}")
  public Result<QuestionResponse> update(
      @PathVariable Long id, @Valid @RequestBody QuestionRequest request) {
    return Result.success(questionAdminService.update(id, request));
  }

  @PatchMapping({"/{id}/isOn", "/{id}/status"})
  public Result<QuestionResponse> updateIsOn(
      @PathVariable Long id, @Valid @RequestBody QuestionOnRequest request) {
    return Result.success(questionAdminService.updateIsOn(id, request.isOn()));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    questionAdminService.delete(id);
    return Result.success();
  }
}
