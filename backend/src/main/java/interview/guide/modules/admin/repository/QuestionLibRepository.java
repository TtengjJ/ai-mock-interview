package interview.guide.modules.admin.repository;

import interview.guide.modules.admin.model.QuestionLibEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionLibRepository extends JpaRepository<QuestionLibEntity, Long> {
  List<QuestionLibEntity> findByJobIdOrderBySortOrderAscIdAsc(Long jobId);

  List<QuestionLibEntity> findByJobIdAndIsOnOrderBySortOrderAscIdAsc(Long jobId, Integer isOn);
}
