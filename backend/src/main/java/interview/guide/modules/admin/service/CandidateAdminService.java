package interview.guide.modules.admin.service;

import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import interview.guide.modules.admin.model.AdminDtos.CandidateRequest;
import interview.guide.modules.admin.model.AdminDtos.CandidateResponse;
import interview.guide.modules.admin.model.CandidateEntity;
import interview.guide.modules.admin.repository.CandidateRepository;
import interview.guide.modules.admin.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CandidateAdminService {

  private final CandidateRepository candidateRepository;
  private final JobRepository jobRepository;
  private final PasswordEncoder passwordEncoder;

  public List<CandidateResponse> list() {
    return candidateRepository.findAll().stream().map(this::toResponse).toList();
  }

  public CandidateResponse get(Long id) {
    return toResponse(findOrThrow(id));
  }

  @Transactional
  public CandidateResponse create(CandidateRequest request) {
    if (candidateRepository.existsByMobile(request.mobile())) {
      throw new BusinessException(ErrorCode.MOBILE_ALREADY_EXISTS);
    }
    if (!StringUtils.hasText(request.password())) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "密码不能为空");
    }
    validateJob(request.jobId());
    CandidateEntity entity = CandidateEntity.builder()
        .realName(request.realName())
        .mobile(request.mobile())
        .email(request.email())
        .sex(request.sex() == null ? 0 : request.sex())
        .password(passwordEncoder.encode(request.password()))
        .jobId(request.jobId())
        .remark(request.remark())
        .build();
    return toResponse(candidateRepository.save(entity));
  }

  @Transactional
  public CandidateResponse update(Long id, CandidateRequest request) {
    CandidateEntity entity = findOrThrow(id);
    if (!entity.getMobile().equals(request.mobile())
        && candidateRepository.existsByMobile(request.mobile())) {
      throw new BusinessException(ErrorCode.MOBILE_ALREADY_EXISTS);
    }
    validateJob(request.jobId());
    entity.setRealName(request.realName());
    entity.setMobile(request.mobile());
    entity.setEmail(request.email());
    if (request.sex() != null) {
      entity.setSex(request.sex());
    }
    if (StringUtils.hasText(request.password())) {
      entity.setPassword(passwordEncoder.encode(request.password()));
    }
    entity.setJobId(request.jobId());
    entity.setRemark(request.remark());
    return toResponse(candidateRepository.save(entity));
  }

  @Transactional
  public void delete(Long id) {
    if (!candidateRepository.existsById(id)) {
      throw new BusinessException(ErrorCode.CANDIDATE_NOT_FOUND);
    }
    candidateRepository.deleteById(id);
  }

  private CandidateEntity findOrThrow(Long id) {
    return candidateRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.CANDIDATE_NOT_FOUND));
  }

  private void validateJob(Long jobId) {
    if (jobId != null && !jobRepository.existsById(jobId)) {
      throw new BusinessException(ErrorCode.JOB_NOT_FOUND);
    }
  }

  private CandidateResponse toResponse(CandidateEntity e) {
    return new CandidateResponse(
        e.getId(), e.getRealName(), e.getMobile(), e.getEmail(), e.getSex(),
        e.getJobId(), e.getRemark(), e.getCreatedAt(), e.getUpdatedAt()
    );
  }
}
