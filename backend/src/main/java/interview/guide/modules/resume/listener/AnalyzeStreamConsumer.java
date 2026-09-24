package interview.guide.modules.resume.listener;

import interview.guide.common.async.AbstractStreamConsumer;
import interview.guide.common.constant.AsyncTaskStreamConstants;
import interview.guide.common.model.AsyncTaskStatus;
import interview.guide.infrastructure.redis.RedisService;
import interview.guide.modules.resume.model.ResumeAnalysisEntity;
import interview.guide.modules.resume.model.ResumeDtos.AnalyzeResult;
import interview.guide.modules.resume.model.ResumeEntity;
import interview.guide.modules.resume.repository.ResumeAnalysisRepository;
import interview.guide.modules.resume.repository.ResumeRepository;
import interview.guide.modules.resume.service.ResumeGradingService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.stream.StreamMessageId;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class AnalyzeStreamConsumer extends AbstractStreamConsumer<AnalyzeTaskPayload> {

  private final ResumeRepository resumeRepository;
  private final ResumeAnalysisRepository analysisRepository;
  private final ResumeGradingService gradingService;
  private final AnalyzeStreamProducer analyzeStreamProducer;

  public AnalyzeStreamConsumer(
      RedisService redisService,
      ResumeRepository resumeRepository,
      ResumeAnalysisRepository analysisRepository,
      ResumeGradingService gradingService,
      AnalyzeStreamProducer analyzeStreamProducer) {
    super(redisService);
    this.resumeRepository = resumeRepository;
    this.analysisRepository = analysisRepository;
    this.gradingService = gradingService;
    this.analyzeStreamProducer = analyzeStreamProducer;
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
  protected String groupName() {
    return AsyncTaskStreamConstants.RESUME_ANALYZE_GROUP_NAME;
  }

  @Override
  protected String consumerPrefix() {
    return AsyncTaskStreamConstants.RESUME_ANALYZE_CONSUMER_PREFIX;
  }

  @Override
  protected String threadName() {
    return "resume-analyze-consumer";
  }

  @Override
  protected AnalyzeTaskPayload parsePayload(StreamMessageId messageId, Map<String, String> data) {
    String resumeIdStr = data.get(AsyncTaskStreamConstants.FIELD_RESUME_ID);
    if (resumeIdStr == null || resumeIdStr.isBlank()) {
      log.warn("无效简历分析消息，缺少 resumeId: messageId={}", messageId);
      return null;
    }
    try {
      Long resumeId = Long.parseLong(resumeIdStr);
      String content = data.getOrDefault(AsyncTaskStreamConstants.FIELD_CONTENT, "");
      int retryCount = parseRetryCount(data);
      return new AnalyzeTaskPayload(resumeId, content, retryCount);
    } catch (NumberFormatException e) {
      log.warn("无效 resumeId: {}", resumeIdStr);
      return null;
    }
  }

  @Override
  protected String payloadIdentifier(AnalyzeTaskPayload payload) {
    return "resumeId=" + payload.resumeId();
  }

  @Override
  protected void markProcessing(AnalyzeTaskPayload payload) {
    Optional<ResumeEntity> opt = resumeRepository.findById(payload.resumeId());
    if (opt.isEmpty()) {
      return;
    }
    ResumeEntity resume = opt.get();
    resume.setAnalyzeStatus(AsyncTaskStatus.PROCESSING);
    resume.setAnalyzeError(null);
    resumeRepository.save(resume);
  }

  @Override
  protected void processBusiness(AnalyzeTaskPayload payload) {
    Optional<ResumeEntity> opt = resumeRepository.findById(payload.resumeId());
    if (opt.isEmpty()) {
      // 实体已删：直接跳过（外层 ACK）
      log.info("简历已删除，跳过分析: resumeId={}", payload.resumeId());
      return;
    }

    String content = payload.content();
    if (content == null || content.isBlank()) {
      content = opt.get().getRawText();
    }

    AnalyzeResult result = gradingService.analyzeResume(content);

    ResumeAnalysisEntity analysis = ResumeAnalysisEntity.builder()
        .resumeId(payload.resumeId())
        .overallScore(result.overallScore())
        .summary(result.summary())
        .strengths(gradingService.toJsonArray(result.strengths()))
        .suggestions(gradingService.toJsonArray(result.suggestions()))
        .build();
    analysisRepository.save(analysis);
  }

  @Override
  protected void markCompleted(AnalyzeTaskPayload payload) {
    resumeRepository.findById(payload.resumeId()).ifPresent(resume -> {
      resume.setAnalyzeStatus(AsyncTaskStatus.COMPLETED);
      resume.setAnalyzeError(null);
      resumeRepository.save(resume);
    });
  }

  @Override
  protected void markFailed(AnalyzeTaskPayload payload, String error) {
    resumeRepository.findById(payload.resumeId()).ifPresent(resume -> {
      resume.setAnalyzeStatus(AsyncTaskStatus.FAILED);
      resume.setAnalyzeError(error);
      resumeRepository.save(resume);
    });
  }

  @Override
  protected void retryMessage(AnalyzeTaskPayload payload, int retryCount) {
    analyzeStreamProducer.sendRetry(payload, retryCount);
  }
}
