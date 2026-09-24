package interview.guide.modules.admin.repository;

import interview.guide.modules.admin.model.InterviewerConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewerConfigRepository extends JpaRepository<InterviewerConfigEntity, Long> {
}
