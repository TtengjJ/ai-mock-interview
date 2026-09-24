package interview.guide.modules.resume.listener;

/**
 * 简历分析 Stream 消息载荷
 */
public record AnalyzeTaskPayload(Long resumeId, String content, int retryCount) {}
