package interview.guide.modules.resume.listener;

import interview.guide.common.async.AbstractStreamProducer;
import interview.guide.common.constant.AsyncTaskStreamConstants;
import interview.guide.common.model.AsyncTaskStatus;
import interview.guide.infrastructure.redis.RedisService;
import interview.guide.modules.resume.model.ResumeEntity;
import interview.guide.modules.resume.repository.ResumeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class AnalyzeStreamProducer extends AbstractStreamProducer<AnalyzeTaskPayload> {

  private final ResumeRepository resumeRepository;

  public AnalyzeStreamProducer(RedisService redisService, ResumeRepository resumeRepository) {
    super(redisService);
    this.resumeRepository = resumeRepository;
  }

  public void sendAnalyzeTask(Long resumeId, String content) {
    sendTask(new AnalyzeTaskPayload(resumeId, content, 0));
  }

  public void sendRetry(AnalyzeTaskPayload payload, int retryCount) {
    sendTask(new AnalyzeTaskPayload(payload.resumeId(), payload.content(), retryCount));
  }

  @Override
  protected String taskDisplayName() {
    return "简历分析";
  }

  @Override
  protected String streamKey() {
    return AsyncTaskStreamConstants.RESUME_ANALYZE_STREAM_KEY;
  }

  @Override
  protected Map<String, String> buildMessage(AnalyzeTaskPayload payload) {
    Map<String, String> map = new HashMap<>();
    map.put(AsyncTaskStreamConstants.FIELD_RESUME_ID, String.valueOf(payload.resumeId()));
    map.put(AsyncTaskStreamConstants.FIELD_CONTENT, payload.content() == null ? "" : payload.content());
    map.put(AsyncTaskStreamConstants.FIELD_RETRY_COUNT, String.valueOf(payload.retryCount()));
    return map;
  }

  @Override
  protected String payloadIdentifier(AnalyzeTaskPayload payload) {
    return "resumeId=" + payload.resumeId();
  }

  @Override
  protected void onSendFailed(AnalyzeTaskPayload payload, String error) {
    resumeRepository.findById(payload.resumeId()).ifPresent(resume -> {
      resume.setAnalyzeStatus(AsyncTaskStatus.FAILED);
      resume.setAnalyzeError(truncateError(error));
      resumeRepository.save(resume);
    });
  }
}
