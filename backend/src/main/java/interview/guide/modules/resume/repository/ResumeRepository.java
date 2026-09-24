package interview.guide.modules.resume.repository;

import interview.guide.modules.resume.model.ResumeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<ResumeEntity, Long> {
  List<ResumeEntity> findByCandidateIdOrderByCreatedAtDesc(Long candidateId);

  Optional<ResumeEntity> findByCandidateIdAndContentHash(Long candidateId, String contentHash);

  Optional<ResumeEntity> findByIdAndCandidateId(Long id, Long candidateId);
}
