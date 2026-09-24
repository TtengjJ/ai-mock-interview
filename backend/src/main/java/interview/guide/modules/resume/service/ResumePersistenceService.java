package interview.guide.modules.resume.service;

import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import interview.guide.modules.resume.model.ResumeEntity;
import interview.guide.modules.resume.repository.ResumeAnalysisRepository;
import interview.guide.modules.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResumePersistenceService {

  private final ResumeRepository resumeRepository;
  private final ResumeAnalysisRepository analysisRepository;

  @Transactional
  public ResumeEntity save(ResumeEntity entity) {
    return resumeRepository.save(entity);
  }

  @Transactional
  public String deleteOwned(Long candidateId, Long resumeId) {
    ResumeEntity resume = resumeRepository.findByIdAndCandidateId(resumeId, candidateId)
        .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));
    String storageKey = resume.getStorageKey();
    analysisRepository.deleteByResumeId(resumeId);
    resumeRepository.delete(resume);
    return storageKey;
  }
}
