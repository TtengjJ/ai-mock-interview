package interview.guide.modules.admin.service;

import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import interview.guide.modules.admin.model.AdminDtos.QuestionRequest;
import interview.guide.modules.admin.model.AdminDtos.QuestionResponse;
import interview.guide.modules.admin.model.JobEntity;
import interview.guide.modules.admin.model.QuestionLibEntity;
import interview.guide.modules.admin.repository.JobRepository;
import interview.guide.modules.admin.repository.QuestionLibRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionAdminService {

  private final QuestionLibRepository questionLibRepository;
  private final JobRepository jobRepository;

  public List<QuestionResponse> list(Long jobId) {
    List<QuestionLibEntity> list = jobId == null
        ? questionLibRepository.findAll()
        : questionLibRepository.findByJobIdOrderBySortOrderAscIdAsc(jobId);
    Map<Long, String> jobNames = loadJobNames(list);
    return list.stream().map(e -> toResponse(e, jobNames)).toList();
  }

  public QuestionResponse get(Long id) {
    QuestionLibEntity entity = findOrThrow(id);
    return toResponse(entity, loadJobNames(List.of(entity)));
  }

  @Transactional
  public QuestionResponse create(QuestionRequest request) {
    ensureJobExists(request.jobId());
    QuestionLibEntity entity = QuestionLibEntity.builder()
        .jobId(request.jobId())
        .question(request.question())
        .referenceAnswer(request.referenceAnswer())
        .isOn(request.isOn() == null ? 1 : request.isOn())
        .sortOrder(request.sortOrder() == null ? 0 : request.sortOrder())
        .build();
    QuestionLibEntity saved = questionLibRepository.save(entity);
    return toResponse(saved, loadJobNames(List.of(saved)));
  }

  @Transactional
  public QuestionResponse update(Long id, QuestionRequest request) {
    QuestionLibEntity entity = findOrThrow(id);
    ensureJobExists(request.jobId());
    entity.setJobId(request.jobId());
    entity.setQuestion(request.question());
    entity.setReferenceAnswer(request.referenceAnswer());
    if (request.isOn() != null) {
      entity.setIsOn(request.isOn());
    }
    if (request.sortOrder() != null) {
      entity.setSortOrder(request.sortOrder());
    }
    QuestionLibEntity saved = questionLibRepository.save(entity);
    return toResponse(saved, loadJobNames(List.of(saved)));
  }

  @Transactional
  public QuestionResponse updateIsOn(Long id, Integer isOn) {
    if (isOn == null || (isOn != 0 && isOn != 1)) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "isOn 仅支持 0/1");
    }
    QuestionLibEntity entity = findOrThrow(id);
    entity.setIsOn(isOn);
    QuestionLibEntity saved = questionLibRepository.save(entity);
    return toResponse(saved, loadJobNames(List.of(saved)));
  }

  @Transactional
  public void delete(Long id) {
    if (!questionLibRepository.existsById(id)) {
      throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND);
    }
    questionLibRepository.deleteById(id);
  }

  private QuestionLibEntity findOrThrow(Long id) {
    return questionLibRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));
  }

  private void ensureJobExists(Long jobId) {
    if (!jobRepository.existsById(jobId)) {
      throw new BusinessException(ErrorCode.JOB_NOT_FOUND);
    }
  }

  private Map<Long, String> loadJobNames(List<QuestionLibEntity> questions) {
    Set<Long> ids = questions.stream()
        .map(QuestionLibEntity::getJobId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    if (ids.isEmpty()) {
      return Map.of();
    }
    Map<Long, String> map = new HashMap<>();
    for (JobEntity job : jobRepository.findAllById(ids)) {
      map.put(job.getId(), job.getJobName());
    }
    return map;
  }

  private QuestionResponse toResponse(QuestionLibEntity e, Map<Long, String> jobNames) {
    return new QuestionResponse(
        e.getId(), e.getJobId(), jobNames.get(e.getJobId()), e.getQuestion(),
        e.getReferenceAnswer(), e.getIsOn(), e.getSortOrder(),
        e.getCreatedAt(), e.getUpdatedAt()
    );
  }
}
