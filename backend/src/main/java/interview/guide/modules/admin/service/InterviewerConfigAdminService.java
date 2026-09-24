package interview.guide.modules.admin.service;

import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import interview.guide.modules.admin.model.AdminDtos.InterviewerConfigRequest;
import interview.guide.modules.admin.model.AdminDtos.InterviewerConfigResponse;
import interview.guide.modules.admin.model.InterviewerConfigEntity;
import interview.guide.modules.admin.repository.InterviewerConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewerConfigAdminService {

  private final InterviewerConfigRepository repository;

  public List<InterviewerConfigResponse> list() {
    return repository.findAll().stream().map(this::toResponse).toList();
  }

  public InterviewerConfigResponse get(Long id) {
    return toResponse(findOrThrow(id));
  }

  @Transactional
  public InterviewerConfigResponse create(InterviewerConfigRequest request) {
    InterviewerConfigEntity entity = InterviewerConfigEntity.builder()
        .aiName(request.aiName())
        .systemPrompt(request.systemPrompt())
        .build();
    return toResponse(repository.save(entity));
  }

  @Transactional
  public InterviewerConfigResponse update(Long id, InterviewerConfigRequest request) {
    InterviewerConfigEntity entity = findOrThrow(id);
    entity.setAiName(request.aiName());
    entity.setSystemPrompt(request.systemPrompt());
    return toResponse(repository.save(entity));
  }

  @Transactional
  public void delete(Long id) {
    if (!repository.existsById(id)) {
      throw new BusinessException(ErrorCode.INTERVIEWER_CONFIG_NOT_FOUND);
    }
    repository.deleteById(id);
  }

  private InterviewerConfigEntity findOrThrow(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEWER_CONFIG_NOT_FOUND));
  }

  private InterviewerConfigResponse toResponse(InterviewerConfigEntity e) {
    return new InterviewerConfigResponse(
        e.getId(), e.getAiName(), e.getSystemPrompt(), e.getCreatedAt(), e.getUpdatedAt()
    );
  }
}
