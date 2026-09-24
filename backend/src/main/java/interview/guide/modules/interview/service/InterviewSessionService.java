package interview.guide.modules.interview.service;

import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import interview.guide.common.model.AsyncTaskStatus;
import interview.guide.modules.admin.model.CandidateEntity;
import interview.guide.modules.admin.model.JobEntity;
import interview.guide.modules.admin.repository.CandidateRepository;
import interview.guide.modules.admin.repository.JobRepository;
import interview.guide.modules.interview.listener.EvaluateStreamProducer;
import interview.guide.modules.interview.model.InterviewAnswerEntity;
import interview.guide.modules.interview.model.InterviewDtos.CreateSessionRequest;
import interview.guide.modules.interview.model.InterviewDtos.JobOptionResponse;
import interview.guide.modules.interview.model.InterviewDtos.PageResponse;
import interview.guide.modules.interview.model.InterviewDtos.QuestionView;
import interview.guide.modules.interview.model.InterviewDtos.SessionDetailResponse;
import interview.guide.modules.interview.model.InterviewDtos.SessionSummaryResponse;
import interview.guide.modules.interview.model.InterviewSessionEntity;
import interview.guide.modules.interview.model.InterviewSessionStatus;
import interview.guide.modules.interview.repository.InterviewAnswerRepository;
import interview.guide.modules.interview.repository.InterviewSessionRepository;
import interview.guide.modules.resume.model.ResumeAnalysisEntity;
import interview.guide.modules.resume.model.ResumeEntity;
import interview.guide.modules.resume.repository.ResumeAnalysisRepository;
import interview.guide.modules.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewSessionService {

  private final InterviewSessionRepository sessionRepository;
  private final InterviewAnswerRepository answerRepository;
  private final InterviewPersistenceService persistenceService;
  private final JobRepository jobRepository;
  private final CandidateRepository candidateRepository;
  private final ResumeRepository resumeRepository;
  private final ResumeAnalysisRepository resumeAnalysisRepository;
  private final InterviewQuestionService questionService;
  private final InterviewEvaluationService evaluationService;
  private final EvaluateStreamProducer evaluateStreamProducer;

  public List<JobOptionResponse> listOpenJobs() {
    return jobRepository.findByStatusOrderByIdDesc(1).stream()
        .map(j -> new JobOptionResponse(j.getId(), j.getJobName(), j.getJobDesc()))
        .toList();
  }

  /**
   * 创建会话：DB 短事务与 LLM 出题分离（事务内不调 LLM）。
   */
  public SessionDetailResponse createSession(Long candidateId, CreateSessionRequest request) {
    JobEntity job = jobRepository.findById(request.jobId())
        .orElseThrow(() -> new BusinessException(ErrorCode.JOB_NOT_FOUND));
    if (job.getStatus() == null || job.getStatus() != 1) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "岗位未开启");
    }

    String resumeSummary = null;
    Long resumeId = request.resumeId();
    if (resumeId != null) {
      ResumeEntity resume = resumeRepository.findById(resumeId)
          .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));
      if (!candidateId.equals(resume.getCandidateId())) {
        throw new BusinessException(ErrorCode.FORBIDDEN, "无权使用该简历");
      }
      resumeSummary = resumeAnalysisRepository.findFirstByResumeIdOrderByAnalyzedAtDesc(resumeId)
          .map(ResumeAnalysisEntity::getSummary)
          .orElse(truncate(resume.getRawText(), 1500));
    }

    InterviewSessionEntity session = persistenceService.saveSession(
        InterviewSessionEntity.builder()
            .candidateId(candidateId)
            .jobId(job.getId())
            .resumeId(resumeId)
            .status(InterviewSessionStatus.IN_PROGRESS)
            .totalQuestions(InterviewQuestionService.DEFAULT_QUESTION_COUNT)
            .currentIndex(0)
            .build());

    List<InterviewAnswerEntity> questions = questionService.buildQuestions(
        session.getId(), job, resumeSummary, InterviewQuestionService.DEFAULT_QUESTION_COUNT);
    persistenceService.saveQuestions(session, questions);

    log.info("面试会话已创建: sessionId={}, jobId={}, questionCount={}",
        session.getId(), job.getId(), questions.size());
    return toDetail(session, answerRepository.findBySessionIdOrderByQuestionIndexAsc(session.getId()));
  }

  public List<SessionSummaryResponse> listMySessions(Long candidateId) {
    Map<Long, String> jobNames = loadJobNames();
    return sessionRepository.findByCandidateIdOrderByCreatedAtDesc(candidateId).stream()
        .map(s -> toSummary(s, jobNames.get(s.getJobId())))
        .toList();
  }

  public SessionDetailResponse getMySession(Long candidateId, Long sessionId) {
    InterviewSessionEntity session = sessionRepository.findByIdAndCandidateId(sessionId, candidateId)
        .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_SESSION_NOT_FOUND));
    return toDetail(session, answerRepository.findBySessionIdOrderByQuestionIndexAsc(sessionId));
  }

  public SessionDetailResponse submitAnswer(Long candidateId, Long sessionId, String answerText) {
    InterviewSessionEntity session = requireInProgress(candidateId, sessionId);
    InterviewAnswerEntity current = currentAnswer(session);
    if (current.getAnswerText() != null && !current.getAnswerText().isBlank()) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "本题已作答，请生成追问或进入下一题");
    }
    current.setAnswerText(answerText.trim());
    persistenceService.saveAnswer(current);
    return toDetail(session, answerRepository.findBySessionIdOrderByQuestionIndexAsc(sessionId));
  }

  public SessionDetailResponse generateFollowUp(Long candidateId, Long sessionId) {
    InterviewSessionEntity session = requireInProgress(candidateId, sessionId);
    InterviewAnswerEntity current = currentAnswer(session);
    if (current.getAnswerText() == null || current.getAnswerText().isBlank()) {
      throw new BusinessException(ErrorCode.INTERVIEW_ANSWER_REQUIRED);
    }
    if (current.getFollowUpQuestion() != null && !current.getFollowUpQuestion().isBlank()) {
      throw new BusinessException(ErrorCode.INTERVIEW_FOLLOW_UP_EXISTS);
    }
    JobEntity job = jobRepository.findById(session.getJobId())
        .orElseThrow(() -> new BusinessException(ErrorCode.JOB_NOT_FOUND));
    // LLM 在事务外
    String followUp = questionService.generateFollowUp(
        current.getQuestionText(), current.getAnswerText(), job.getJobName());
    current.setFollowUpQuestion(followUp);
    persistenceService.saveAnswer(current);
    return toDetail(session, answerRepository.findBySessionIdOrderByQuestionIndexAsc(sessionId));
  }

  public SessionDetailResponse submitFollowUpAnswer(Long candidateId, Long sessionId, String answerText) {
    InterviewSessionEntity session = requireInProgress(candidateId, sessionId);
    InterviewAnswerEntity current = currentAnswer(session);
    if (current.getFollowUpQuestion() == null || current.getFollowUpQuestion().isBlank()) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "尚未生成追问");
    }
    if (current.getFollowUpAnswer() != null && !current.getFollowUpAnswer().isBlank()) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "追问已作答");
    }
    current.setFollowUpAnswer(answerText.trim());
    persistenceService.saveAnswer(current);
    return toDetail(session, answerRepository.findBySessionIdOrderByQuestionIndexAsc(sessionId));
  }

  public SessionDetailResponse nextQuestion(Long candidateId, Long sessionId) {
    InterviewSessionEntity session = requireInProgress(candidateId, sessionId);
    InterviewAnswerEntity current = currentAnswer(session);
    if (current.getAnswerText() == null || current.getAnswerText().isBlank()) {
      throw new BusinessException(ErrorCode.INTERVIEW_ANSWER_REQUIRED);
    }
    if (current.getFollowUpQuestion() != null
        && !current.getFollowUpQuestion().isBlank()
        && (current.getFollowUpAnswer() == null || current.getFollowUpAnswer().isBlank())) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "请先完成追问作答");
    }
    if (session.getCurrentIndex() >= session.getTotalQuestions() - 1) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "已是最后一题，请结束面试");
    }
    session.setCurrentIndex(session.getCurrentIndex() + 1);
    persistenceService.saveSession(session);
    return toDetail(session, answerRepository.findBySessionIdOrderByQuestionIndexAsc(sessionId));
  }

  public SessionDetailResponse complete(Long candidateId, Long sessionId) {
    InterviewSessionEntity session = requireInProgress(candidateId, sessionId);
    List<InterviewAnswerEntity> answers =
        answerRepository.findBySessionIdOrderByQuestionIndexAsc(sessionId);
    boolean anyAnswered = answers.stream()
        .anyMatch(a -> a.getAnswerText() != null && !a.getAnswerText().isBlank());
    if (!anyAnswered) {
      throw new BusinessException(ErrorCode.INTERVIEW_ANSWER_REQUIRED, "至少回答一题后再结束");
    }
    session.setStatus(InterviewSessionStatus.COMPLETED);
    session.setCompletedAt(LocalDateTime.now());
    session.setEvaluateStatus(AsyncTaskStatus.PENDING);
    persistenceService.saveSession(session);
    evaluateStreamProducer.sendEvaluateTask(session.getId());
    return toDetail(session, answers);
  }

  public PageResponse<SessionDetailResponse> adminList(int page, int size) {
    PageRequest pr = PageRequest.of(Math.max(page - 1, 0), Math.min(Math.max(size, 1), 100));
    Page<InterviewSessionEntity> result = sessionRepository.findAllByOrderByCreatedAtDesc(pr);
    Map<Long, String> jobNames = loadJobNames();
    Map<Long, String> candidateNames = loadCandidateNames();
    List<SessionDetailResponse> items = result.getContent().stream()
        .map(s -> {
          List<InterviewAnswerEntity> answers =
              answerRepository.findBySessionIdOrderByQuestionIndexAsc(s.getId());
          return toDetail(s, answers, jobNames.get(s.getJobId()), candidateNames.get(s.getCandidateId()));
        })
        .toList();
    return new PageResponse<>(items, result.getTotalElements(), page, size);
  }

  public SessionDetailResponse adminGet(Long sessionId) {
    InterviewSessionEntity session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_SESSION_NOT_FOUND));
    String jobName = jobRepository.findById(session.getJobId()).map(JobEntity::getJobName).orElse(null);
    String candidateName = candidateRepository.findById(session.getCandidateId())
        .map(CandidateEntity::getRealName).orElse(null);
    return toDetail(
        session,
        answerRepository.findBySessionIdOrderByQuestionIndexAsc(sessionId),
        jobName,
        candidateName);
  }

  private InterviewSessionEntity requireInProgress(Long candidateId, Long sessionId) {
    InterviewSessionEntity session = sessionRepository.findByIdAndCandidateId(sessionId, candidateId)
        .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_SESSION_NOT_FOUND));
    if (session.getStatus() != InterviewSessionStatus.IN_PROGRESS) {
      throw new BusinessException(ErrorCode.INTERVIEW_ALREADY_COMPLETED);
    }
    return session;
  }

  private InterviewAnswerEntity currentAnswer(InterviewSessionEntity session) {
    return answerRepository.findBySessionIdAndQuestionIndex(session.getId(), session.getCurrentIndex())
        .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_QUESTION_NOT_FOUND));
  }

  private SessionSummaryResponse toSummary(InterviewSessionEntity s, String jobName) {
    return new SessionSummaryResponse(
        s.getId(),
        s.getJobId(),
        jobName,
        s.getResumeId(),
        s.getStatus().name(),
        s.getEvaluateStatus() == null ? null : s.getEvaluateStatus().name(),
        s.getOverallScore(),
        s.getTotalQuestions(),
        s.getCurrentIndex(),
        s.getCreatedAt(),
        s.getCompletedAt());
  }

  private SessionDetailResponse toDetail(InterviewSessionEntity s, List<InterviewAnswerEntity> answers) {
    String jobName = jobRepository.findById(s.getJobId()).map(JobEntity::getJobName).orElse(null);
    String candidateName = candidateRepository.findById(s.getCandidateId())
        .map(CandidateEntity::getRealName).orElse(null);
    return toDetail(s, answers, jobName, candidateName);
  }

  private SessionDetailResponse toDetail(
      InterviewSessionEntity s,
      List<InterviewAnswerEntity> answers,
      String jobName,
      String candidateName) {
    List<QuestionView> views = answers.stream().map(a -> {
      boolean answered = a.getAnswerText() != null && !a.getAnswerText().isBlank();
      boolean fuGen = a.getFollowUpQuestion() != null && !a.getFollowUpQuestion().isBlank();
      boolean fuAns = a.getFollowUpAnswer() != null && !a.getFollowUpAnswer().isBlank();
      return new QuestionView(
          a.getQuestionIndex(),
          a.getQuestionText(),
          a.getSource().name(),
          a.getAnswerText(),
          a.getFollowUpQuestion(),
          a.getFollowUpAnswer(),
          answered,
          fuGen,
          fuAns);
    }).toList();

    return new SessionDetailResponse(
        s.getId(),
        s.getCandidateId(),
        candidateName,
        s.getJobId(),
        jobName,
        s.getResumeId(),
        s.getStatus().name(),
        s.getEvaluateStatus() == null ? null : s.getEvaluateStatus().name(),
        s.getEvaluateError(),
        s.getOverallScore(),
        s.getOverallFeedback(),
        evaluationService.fromJsonArray(s.getStrengthsJson()),
        evaluationService.fromJsonArray(s.getImprovementsJson()),
        s.getTotalQuestions(),
        s.getCurrentIndex(),
        views,
        s.getCreatedAt(),
        s.getCompletedAt());
  }

  private Map<Long, String> loadJobNames() {
    Map<Long, String> map = new HashMap<>();
    jobRepository.findAll().forEach(j -> map.put(j.getId(), j.getJobName()));
    return map;
  }

  private Map<Long, String> loadCandidateNames() {
    Map<Long, String> map = new HashMap<>();
    candidateRepository.findAll().forEach(c -> map.put(c.getId(), c.getRealName()));
    return map;
  }

  private String truncate(String text, int max) {
    if (text == null) {
      return null;
    }
    return text.length() <= max ? text : text.substring(0, max);
  }
}
