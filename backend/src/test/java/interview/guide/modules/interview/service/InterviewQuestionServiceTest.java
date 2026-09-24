package interview.guide.modules.interview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import interview.guide.modules.admin.model.JobEntity;
import interview.guide.modules.admin.model.QuestionLibEntity;
import interview.guide.modules.admin.repository.QuestionLibRepository;
import interview.guide.modules.interview.model.InterviewAnswerEntity;
import interview.guide.modules.interview.model.QuestionSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("面试出题服务")
class InterviewQuestionServiceTest {

  @Mock
  private QuestionLibRepository questionLibRepository;

  @Mock
  private ChatClient chatClient;

  @Mock
  private ObjectMapper objectMapper;

  @InjectMocks
  private InterviewQuestionService questionService;

  @Nested
  @DisplayName("题库足够时")
  class WhenBankEnough {

    @Test
    @DisplayName("全部来自题库且不调用 LLM")
    void shouldUseBankOnlyWithoutLlm() {
      Long jobId = 1L;
      List<QuestionLibEntity> bank = new ArrayList<>();
      for (int i = 0; i < 5; i++) {
        bank.add(QuestionLibEntity.builder()
            .id((long) (i + 1))
            .jobId(jobId)
            .question("题库题目" + (i + 1))
            .referenceAnswer("参考" + (i + 1))
            .isOn(1)
            .sortOrder(i)
            .build());
      }
      when(questionLibRepository.findByJobIdAndIsOnOrderBySortOrderAscIdAsc(jobId, 1))
          .thenReturn(bank);

      JobEntity job = JobEntity.builder().id(jobId).jobName("Java").status(1).build();
      List<InterviewAnswerEntity> result =
          questionService.buildQuestions(100L, job, null, 5);

      assertThat(result).hasSize(5);
      assertThat(result).allMatch(a -> a.getSource() == QuestionSource.BANK);
      assertThat(result.get(0).getQuestionText()).isEqualTo("题库题目1");
      verifyNoInteractions(chatClient);
      verify(questionLibRepository).findByJobIdAndIsOnOrderBySortOrderAscIdAsc(jobId, 1);
    }
  }
}
