package interview.guide.modules.interview.listener;

import interview.guide.common.async.AbstractStreamProducer;
import interview.guide.common.constant.AsyncTaskStreamConstants;
import interview.guide.common.model.AsyncTaskStatus;
import interview.guide.infrastructure.redis.RedisService;
import interview.guide.modules.interview.model.InterviewSessionStatus;
import interview.guide.modules.interview.repository.InterviewSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class EvaluateStreamProducer extends AbstractStreamProducer<EvaluateTaskPayload> {

  private final InterviewSessionRepository sessionRepository;

  public EvaluateStreamProducer(RedisService redisService, InterviewSessionRepository sessionRepository) {
    super(redisService);
    this.sessionRepository = sessionRepository;
  }

  public void sendEvaluateTask(Long sessionId) {
    sendTask(new EvaluateTaskPayload(sessionId, 0));
  }

  public void sendRetry(EvaluateTaskPayload payload, int retryCount) {
    sendTask(new EvaluateTaskPayload(payload.sessionId(), retryCount));
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
  protected Map<String, String> buildMessage(EvaluateTaskPayload payload) {
    Map<String, String> map = new HashMap<>();
    map.put(AsyncTaskStreamConstants.FIELD_SESSION_ID, String.valueOf(payload.sessionId()));
    map.put(AsyncTaskStreamConstants.FIELD_RETRY_COUNT, String.valueOf(payload.retryCount()));
    return map;
  }

  @Override
  protected String payloadIdentifier(EvaluateTaskPayload payload) {
    return "sessionId=" + payload.sessionId();
  }

  @Override
  protected void onSendFailed(EvaluateTaskPayload payload, String error) {
    sessionRepository.findById(payload.sessionId()).ifPresent(session -> {
      session.setStatus(InterviewSessionStatus.EVAL_FAILED);
      session.setEvaluateStatus(AsyncTaskStatus.FAILED);
      session.setEvaluateError(truncateError(error));
      sessionRepository.save(session);
    });
  }
}
