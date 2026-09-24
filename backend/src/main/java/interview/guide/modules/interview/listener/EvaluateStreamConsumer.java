package interview.guide.modules.interview.listener;

import interview.guide.common.async.AbstractStreamConsumer;
import interview.guide.common.constant.AsyncTaskStreamConstants;
import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import interview.guide.common.model.AsyncTaskStatus;
import interview.guide.infrastructure.redis.RedisService;
import interview.guide.modules.admin.model.JobEntity;
import interview.guide.modules.admin.repository.JobRepository;
import interview.guide.modules.interview.model.InterviewAnswerEntity;
import interview.guide.modules.interview.model.InterviewSessionEntity;
import interview.guide.modules.interview.model.InterviewSessionStatus;
import interview.guide.modules.interview.repository.InterviewAnswerRepository;
import interview.guide.modules.interview.repository.InterviewSessionRepository;
import interview.guide.modules.interview.service.InterviewEvaluationService;
import interview.guide.modules.interview.service.InterviewEvaluationService.EvaluationResult;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.stream.StreamMessageId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class EvaluateStreamConsumer extends AbstractStreamConsumer<EvaluateTaskPayload> {

  private final InterviewSessionRepository sessionRepository;
  private final InterviewAnswerRepository answerRepository;
  private final JobRepository jobRepository;
  private final InterviewEvaluationService evaluationService;
  private final EvaluateStreamProducer evaluateStreamProducer;

  public EvaluateStreamConsumer(
      RedisService redisService,
      InterviewSessionRepository sessionRepository,
      InterviewAnswerRepository answerRepository,
      JobRepository jobRepository,
      InterviewEvaluationService evaluationService,
      EvaluateStreamProducer evaluateStreamProducer) {
    super(redisService);
    this.sessionRepository = sessionRepository;
    this.answerRepository = answerRepository;
    this.jobRepository = jobRepository;
    this.evaluationService = evaluationService;
    this.evaluateStreamProducer = evaluateStreamProducer;
  }

  @Override
  protected String taskDisplayName() {
    return "面试评估";
  }

  @Override
  protected String streamKey() {
    return AsyncTaskStreamConstants.INTERVIEW_EVALUATE_STREAM_KEY;
  }

  @Override
  protected String groupName() {
    return AsyncTaskStreamConstants.INTERVIEW_EVALUATE_GROUP_NAME;
  }

  @Override
  protected String consumerPrefix() {
    return AsyncTaskStreamConstants.INTERVIEW_EVALUATE_CONSUMER_PREFIX;
  }

  @Override
  protected String threadName() {
    return "interview-evaluate-consumer";
  }

  @Override
  protected EvaluateTaskPayload parsePayload(StreamMessageId messageId, Map<String, String> data) {
    String sessionIdStr = data.get(AsyncTaskStreamConstants.FIELD_SESSION_ID);
    if (sessionIdStr == null || sessionIdStr.isBlank()) {
      log.warn("无效面试评估消息，缺少 sessionId: messageId={}", messageId);
      return null;
    }
    try {
      Long sessionId = Long.parseLong(sessionIdStr);
      int retryCount = parseRetryCount(data);
      return new EvaluateTaskPayload(sessionId, retryCount);
    } catch (NumberFormatException e) {
      log.warn("无效 sessionId: {}", sessionIdStr);
      return null;
    }
  }

  @Override
  protected String payloadIdentifier(EvaluateTaskPayload payload) {
    return "sessionId=" + payload.sessionId();
  }

  @Override
  protected void markProcessing(EvaluateTaskPayload payload) {
    sessionRepository.findById(payload.sessionId()).ifPresent(session -> {
      session.setStatus(InterviewSessionStatus.EVALUATING);
      session.setEvaluateStatus(AsyncTaskStatus.PROCESSING);
      session.setEvaluateError(null);
      sessionRepository.save(session);
    });
  }

  @Override
  protected void processBusiness(EvaluateTaskPayload payload) {
    Optional<InterviewSessionEntity> opt = sessionRepository.findById(payload.sessionId());
    if (opt.isEmpty()) {
      log.info("面试会话已删除，跳过评估: sessionId={}", payload.sessionId());
      return;
    }
    InterviewSessionEntity session = opt.get();
    JobEntity job = jobRepository.findById(session.getJobId())
        .orElseThrow(() -> new BusinessException(ErrorCode.JOB_NOT_FOUND));
    List<InterviewAnswerEntity> answers =
        answerRepository.findBySessionIdOrderByQuestionIndexAsc(session.getId());
    EvaluationResult result = evaluationService.evaluate(job, answers);

    session.setOverallScore(result.overallScore());
    session.setOverallFeedback(result.overallFeedback());
    session.setStrengthsJson(evaluationService.toJsonArray(result.strengths()));
    session.setImprovementsJson(evaluationService.toJsonArray(result.improvements()));
    sessionRepository.save(session);
  }

  @Override
  protected void markCompleted(EvaluateTaskPayload payload) {
    sessionRepository.findById(payload.sessionId()).ifPresent(session -> {
      session.setStatus(InterviewSessionStatus.EVALUATED);
      session.setEvaluateStatus(AsyncTaskStatus.COMPLETED);
      session.setEvaluateError(null);
      sessionRepository.save(session);
    });
  }

  @Override
  protected void markFailed(EvaluateTaskPayload payload, String error) {
    sessionRepository.findById(payload.sessionId()).ifPresent(session -> {
      session.setStatus(InterviewSessionStatus.EVAL_FAILED);
      session.setEvaluateStatus(AsyncTaskStatus.FAILED);
      session.setEvaluateError(error);
      sessionRepository.save(session);
    });
  }

  @Override
  protected void retryMessage(EvaluateTaskPayload payload, int retryCount) {
    evaluateStreamProducer.sendRetry(payload, retryCount);
  }
}
