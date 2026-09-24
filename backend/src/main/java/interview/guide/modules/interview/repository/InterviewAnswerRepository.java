package interview.guide.modules.interview.repository;

import interview.guide.modules.interview.model.InterviewAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswerEntity, Long> {

  List<InterviewAnswerEntity> findBySessionIdOrderByQuestionIndexAsc(Long sessionId);

  Optional<InterviewAnswerEntity> findBySessionIdAndQuestionIndex(Long sessionId, Integer questionIndex);

  void deleteBySessionId(Long sessionId);
}
