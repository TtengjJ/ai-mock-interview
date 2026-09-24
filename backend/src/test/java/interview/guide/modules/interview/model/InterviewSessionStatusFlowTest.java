package interview.guide.modules.interview.model;

import interview.guide.common.model.AsyncTaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("面试评估状态流转")
class InterviewSessionStatusFlowTest {

  @Test
  @DisplayName("正常路径：进行中 → 已结束 → 评估中 → 评估完成")
  void happyPath() {
    InterviewSessionEntity session = InterviewSessionEntity.builder()
        .status(InterviewSessionStatus.IN_PROGRESS)
        .build();

    // 结束面试
    session.setStatus(InterviewSessionStatus.COMPLETED);
    session.setEvaluateStatus(AsyncTaskStatus.PENDING);
    assertThat(session.getStatus()).isEqualTo(InterviewSessionStatus.COMPLETED);
    assertThat(session.getEvaluateStatus()).isEqualTo(AsyncTaskStatus.PENDING);

    // Stream Consumer 开始
    session.setStatus(InterviewSessionStatus.EVALUATING);
    session.setEvaluateStatus(AsyncTaskStatus.PROCESSING);
    assertThat(session.getStatus()).isEqualTo(InterviewSessionStatus.EVALUATING);

    // 评估成功
    session.setStatus(InterviewSessionStatus.EVALUATED);
    session.setEvaluateStatus(AsyncTaskStatus.COMPLETED);
    session.setOverallScore(85);
    assertThat(session.getStatus()).isEqualTo(InterviewSessionStatus.EVALUATED);
    assertThat(session.getEvaluateStatus()).isEqualTo(AsyncTaskStatus.COMPLETED);
    assertThat(session.getOverallScore()).isEqualTo(85);
  }

  @Test
  @DisplayName("失败路径：评估中 → 评估失败")
  void evalFailedPath() {
    InterviewSessionEntity session = InterviewSessionEntity.builder()
        .status(InterviewSessionStatus.EVALUATING)
        .evaluateStatus(AsyncTaskStatus.PROCESSING)
        .build();

    session.setStatus(InterviewSessionStatus.EVAL_FAILED);
    session.setEvaluateStatus(AsyncTaskStatus.FAILED);
    session.setEvaluateError("AI timeout");

    assertThat(session.getStatus()).isEqualTo(InterviewSessionStatus.EVAL_FAILED);
    assertThat(session.getEvaluateStatus()).isEqualTo(AsyncTaskStatus.FAILED);
    assertThat(session.getEvaluateError()).contains("timeout");
  }
}
