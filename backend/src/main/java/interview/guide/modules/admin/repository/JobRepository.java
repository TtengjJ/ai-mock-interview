package interview.guide.modules.admin.repository;

import interview.guide.modules.admin.model.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<JobEntity, Long> {
  java.util.List<JobEntity> findByStatusOrderByIdDesc(Integer status);
}
