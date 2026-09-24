package interview.guide.modules.interview.repository;

import interview.guide.modules.interview.model.InterviewSessionEntity;
import interview.guide.modules.interview.model.InterviewSessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InterviewSessionRepository extends JpaRepository<InterviewSessionEntity, Long> {

  List<InterviewSessionEntity> findByCandidateIdOrderByCreatedAtDesc(Long candidateId);

  Optional<InterviewSessionEntity> findByIdAndCandidateId(Long id, Long candidateId);

  Page<InterviewSessionEntity> findByStatusInOrderByCreatedAtDesc(
      List<InterviewSessionStatus> statuses, Pageable pageable);

  Page<InterviewSessionEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
