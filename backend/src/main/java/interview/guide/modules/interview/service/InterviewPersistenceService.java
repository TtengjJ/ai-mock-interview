package interview.guide.modules.interview.service;

import interview.guide.modules.interview.model.InterviewAnswerEntity;
import interview.guide.modules.interview.model.InterviewSessionEntity;
import interview.guide.modules.interview.repository.InterviewAnswerRepository;
import interview.guide.modules.interview.repository.InterviewSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewPersistenceService {

  private final InterviewSessionRepository sessionRepository;
  private final InterviewAnswerRepository answerRepository;

  @Transactional
  public InterviewSessionEntity saveSession(InterviewSessionEntity session) {
    return sessionRepository.save(session);
  }

  @Transactional
  public void saveQuestions(InterviewSessionEntity session, List<InterviewAnswerEntity> questions) {
    for (InterviewAnswerEntity q : questions) {
      q.setSessionId(session.getId());
    }
    answerRepository.saveAll(questions);
    session.setTotalQuestions(questions.size());
    sessionRepository.save(session);
  }

  @Transactional
  public InterviewAnswerEntity saveAnswer(InterviewAnswerEntity answer) {
    return answerRepository.save(answer);
  }
}
