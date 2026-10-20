package interview.guide.modules.admin.service;

import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import interview.guide.modules.admin.model.AdminDtos.JobRequest;
import interview.guide.modules.admin.model.AdminDtos.JobResponse;
import interview.guide.modules.admin.model.InterviewerConfigEntity;
import interview.guide.modules.admin.model.JobEntity;
import interview.guide.modules.admin.repository.InterviewerConfigRepository;
import interview.guide.modules.admin.repository.JobRepository;
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
public class JobAdminService {

  private final JobRepository jobRepository;
  private final InterviewerConfigRepository interviewerConfigRepository;

  public List<JobResponse> list() {
    List<JobEntity> all = jobRepository.findAll();
    Map<Long, String> interviewerNames = loadInterviewerNames(all);
    return all.stream().map(e -> toResponse(e, interviewerNames)).toList();
  }

  public JobResponse get(Long id) {
    JobEntity entity = findOrThrow(id);
    return toResponse(entity, loadInterviewerNames(List.of(entity)));
  }

  @Transactional
  public JobResponse create(JobRequest request) {
    validateInterviewerConfig(request.interviewerConfigId());
    JobEntity entity = JobEntity.builder()
        .jobName(request.jobName())
        .jobDesc(request.jobDesc())
        .interviewerConfigId(request.interviewerConfigId())
        .prompt(request.prompt())
        .status(request.status() == null ? 1 : request.status())
        .build();
    JobEntity saved = jobRepository.save(entity);
    return toResponse(saved, loadInterviewerNames(List.of(saved)));
  }

  @Transactional
  public JobResponse update(Long id, JobRequest request) {
    JobEntity entity = findOrThrow(id);
    validateInterviewerConfig(request.interviewerConfigId());
    entity.setJobName(request.jobName());
    entity.setJobDesc(request.jobDesc());
    entity.setInterviewerConfigId(request.interviewerConfigId());
    entity.setPrompt(request.prompt());
    if (request.status() != null) {
      entity.setStatus(request.status());
    }
    JobEntity saved = jobRepository.save(entity);
    return toResponse(saved, loadInterviewerNames(List.of(saved)));
  }

  @Transactional
  public JobResponse updateStatus(Long id, Integer status) {
    if (status == null || (status != 1 && status != 2)) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "status 仅支持 1(开)/2(关)");
    }
    JobEntity entity = findOrThrow(id);
    entity.setStatus(status);
    JobEntity saved = jobRepository.save(entity);
    return toResponse(saved, loadInterviewerNames(List.of(saved)));
  }

  @Transactional
  public void delete(Long id) {
    if (!jobRepository.existsById(id)) {
      throw new BusinessException(ErrorCode.JOB_NOT_FOUND);
    }
    jobRepository.deleteById(id);
  }

  private JobEntity findOrThrow(Long id) {
    return jobRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.JOB_NOT_FOUND));
  }

  private void validateInterviewerConfig(Long configId) {
    if (configId != null && !interviewerConfigRepository.existsById(configId)) {
      throw new BusinessException(ErrorCode.INTERVIEWER_CONFIG_NOT_FOUND);
    }
  }

  private Map<Long, String> loadInterviewerNames(List<JobEntity> jobs) {
    Set<Long> ids = jobs.stream()
        .map(JobEntity::getInterviewerConfigId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    if (ids.isEmpty()) {
      return Map.of();
    }
    Map<Long, String> map = new HashMap<>();
    for (InterviewerConfigEntity config : interviewerConfigRepository.findAllById(ids)) {
      map.put(config.getId(), config.getAiName());
    }
    return map;
  }

  private JobResponse toResponse(JobEntity e, Map<Long, String> interviewerNames) {
    String interviewerName = e.getInterviewerConfigId() == null
        ? null
        : interviewerNames.get(e.getInterviewerConfigId());
    return new JobResponse(
        e.getId(), e.getJobName(), e.getJobDesc(), e.getStatus(),
        e.getInterviewerConfigId(), interviewerName, e.getPrompt(),
        e.getCreatedAt(), e.getUpdatedAt()
    );
  }
}
