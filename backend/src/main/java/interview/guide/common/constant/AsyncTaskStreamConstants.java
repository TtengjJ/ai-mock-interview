package interview.guide.common.constant;

/**
 * 异步任务 Redis Stream 常量（仅简历分析 + 面试评估预留）
 */
public final class AsyncTaskStreamConstants {

  private AsyncTaskStreamConstants() {}

  public static final String FIELD_RETRY_COUNT = "retryCount";
  public static final String FIELD_CONTENT = "content";

  public static final int MAX_RETRY_COUNT = 3;
  public static final int BATCH_SIZE = 10;
  public static final long POLL_INTERVAL_MS = 1000;
  public static final int STREAM_MAX_LEN = 1000;

  public static final String RESUME_ANALYZE_STREAM_KEY = "resume:analyze:stream";
  public static final String RESUME_ANALYZE_GROUP_NAME = "analyze-group";
  public static final String RESUME_ANALYZE_CONSUMER_PREFIX = "analyze-consumer-";
  public static final String FIELD_RESUME_ID = "resumeId";

  /** Phase 3 预留 */
  public static final String INTERVIEW_EVALUATE_STREAM_KEY = "interview:evaluate:stream";
  public static final String INTERVIEW_EVALUATE_GROUP_NAME = "evaluate-group";
  public static final String INTERVIEW_EVALUATE_CONSUMER_PREFIX = "evaluate-consumer-";
  public static final String FIELD_SESSION_ID = "sessionId";
}
