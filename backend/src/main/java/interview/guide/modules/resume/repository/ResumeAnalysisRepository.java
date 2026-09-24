package interview.guide.modules.resume.repository;

import interview.guide.modules.resume.model.ResumeAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysisEntity, Long> {
  Optional<ResumeAnalysisEntity> findFirstByResumeIdOrderByAnalyzedAtDesc(Long resumeId);

  void deleteByResumeId(Long resumeId);
}
