package interview.guide.modules.admin.repository;

import interview.guide.modules.admin.model.CandidateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CandidateRepository extends JpaRepository<CandidateEntity, Long> {
  Optional<CandidateEntity> findByMobile(String mobile);
  boolean existsByMobile(String mobile);
}
