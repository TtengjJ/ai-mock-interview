package interview.guide.modules.admin.service;

import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import interview.guide.modules.admin.model.AdminDtos.JobRequest;
import interview.guide.modules.admin.model.AdminDtos.JobResponse;
import interview.guide.modules.admin.model.JobEntity;
import interview.guide.modules.admin.repository.InterviewerConfigRepository;
import interview.guide.modules.admin.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobAdminService {

  private final JobRepository jobRepository;
  private final InterviewerConfigRepository interviewerConfigRepository;

  public List<JobResponse> list() {
    return jobRepository.findAll().stream().map(this::toResponse).toList();
  }

  public JobResponse get(Long id) {
    return toResponse(findOrThrow(id));
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
    return toResponse(jobRepository.save(entity));
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
    return toResponse(jobRepository.save(entity));
  }

  @Transactional
  public JobResponse updateStatus(Long id, Integer status) {
    if (status == null || (status != 1 && status != 2)) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "status 仅支持 1(开)/2(关)");
    }
    JobEntity entity = findOrThrow(id);
    entity.setStatus(status);
    return toResponse(jobRepository.save(entity));
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

  private JobResponse toResponse(JobEntity e) {
    return new JobResponse(
        e.getId(), e.getJobName(), e.getJobDesc(), e.getStatus(),
        e.getInterviewerConfigId(), e.getPrompt(), e.getCreatedAt(), e.getUpdatedAt()
    );
  }
}
