package interview.guide.modules.interview.model;

/**
 * 面试会话状态机：进行中 → 已结束 → 评估中 → 评估完成/失败
 */
public enum InterviewSessionStatus {
  IN_PROGRESS,
  COMPLETED,
  EVALUATING,
  EVALUATED,
  EVAL_FAILED
}
