package interview.guide.modules.interview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import interview.guide.modules.admin.model.JobEntity;
import interview.guide.modules.admin.model.QuestionLibEntity;
import interview.guide.modules.admin.repository.QuestionLibRepository;
import interview.guide.modules.interview.model.InterviewAnswerEntity;
import interview.guide.modules.interview.model.QuestionSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 出题：岗位题库优先，不足再用 LLM 补题。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewQuestionService {

  public static final int DEFAULT_QUESTION_COUNT = 5;

  private final QuestionLibRepository questionLibRepository;
  private final ChatClient chatClient;
  private final ObjectMapper objectMapper;

  public List<InterviewAnswerEntity> buildQuestions(
      Long sessionId, JobEntity job, String resumeSummary, int targetCount) {
    List<QuestionLibEntity> bank = questionLibRepository
        .findByJobIdAndIsOnOrderBySortOrderAscIdAsc(job.getId(), 1);

    List<InterviewAnswerEntity> answers = new ArrayList<>();
    int index = 0;
    for (QuestionLibEntity q : bank) {
      if (index >= targetCount) {
        break;
      }
      answers.add(InterviewAnswerEntity.builder()
          .sessionId(sessionId)
          .questionIndex(index)
          .questionText(q.getQuestion())
          .referenceAnswer(q.getReferenceAnswer())
          .source(QuestionSource.BANK)
          .build());
      index++;
    }

    int need = targetCount - answers.size();
    if (need > 0) {
      log.info("题库不足，LLM 补题: jobId={}, bank={}, need={}", job.getId(), answers.size(), need);
      List<GeneratedQuestion> generated = generateByLlm(job, resumeSummary, need, answers);
      for (GeneratedQuestion g : generated) {
        answers.add(InterviewAnswerEntity.builder()
            .sessionId(sessionId)
            .questionIndex(index++)
            .questionText(g.question())
            .referenceAnswer(g.referenceAnswer())
            .source(QuestionSource.LLM)
            .build());
      }
    } else {
      log.info("出题全部来自题库: jobId={}, count={}", job.getId(), answers.size());
    }

    if (answers.isEmpty()) {
      throw new BusinessException(ErrorCode.INTERVIEW_QUESTION_GENERATION_FAILED, "无法生成面试题目");
    }
    return answers;
  }

  public String generateFollowUp(String question, String answer, String jobName) {
    try {
      String prompt = """
          你是技术面试官。根据候选人的回答生成【恰好 1 条】简短追问，聚焦澄清或深挖。
          岗位：%s
          原题：%s
          回答：%s
          只输出追问正文，不要编号或其他说明。
          """.formatted(nullToEmpty(jobName), nullToEmpty(question), truncate(answer, 2000));
      String content = chatClient.prompt().user(prompt).call().content();
      if (content == null || content.isBlank()) {
        throw new BusinessException(ErrorCode.INTERVIEW_QUESTION_GENERATION_FAILED, "追问生成失败");
      }
      return content.trim();
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("生成追问失败", e);
      throw new BusinessException(ErrorCode.INTERVIEW_QUESTION_GENERATION_FAILED, "追问生成失败", e);
    }
  }

  private List<GeneratedQuestion> generateByLlm(
      JobEntity job, String resumeSummary, int need, List<InterviewAnswerEntity> existing) {
    try {
      StringBuilder existingText = new StringBuilder();
      for (InterviewAnswerEntity e : existing) {
        existingText.append("- ").append(e.getQuestionText()).append('\n');
      }
      String userPrompt = """
          请为岗位「%s」生成 %d 道技术面试题，严格输出 JSON 数组：
          [{"question":"题目","referenceAnswer":"参考要点"}]
          岗位描述：%s
          评估提示：%s
          简历摘要（可空）：%s
          已有题目（勿重复）：
          %s
          不要输出其他文字。
          """.formatted(
          nullToEmpty(job.getJobName()),
          need,
          nullToEmpty(job.getJobDesc()),
          nullToEmpty(job.getPrompt()),
          nullToEmpty(resumeSummary),
          existingText.isEmpty() ? "无" : existingText.toString());

      String content = chatClient.prompt().user(userPrompt).call().content();
      return parseQuestions(content, need);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("LLM 补题失败: jobId={}", job.getId(), e);
      throw new BusinessException(ErrorCode.INTERVIEW_QUESTION_GENERATION_FAILED, "LLM 补题失败", e);
    }
  }

  private List<GeneratedQuestion> parseQuestions(String content, int need) throws Exception {
    String json = extractJsonArray(content);
    JsonNode arr = objectMapper.readTree(json);
    List<GeneratedQuestion> list = new ArrayList<>();
    if (arr.isArray()) {
      for (JsonNode n : arr) {
        String q = n.path("question").asText(null);
        if (q == null || q.isBlank()) {
          continue;
        }
        list.add(new GeneratedQuestion(q.trim(), n.path("referenceAnswer").asText("")));
        if (list.size() >= need) {
          break;
        }
      }
    }
    if (list.isEmpty()) {
      throw new BusinessException(ErrorCode.INTERVIEW_QUESTION_GENERATION_FAILED, "无法解析 LLM 题目");
    }
    return list;
  }

  private String extractJsonArray(String content) {
    if (content == null) {
      throw new BusinessException(ErrorCode.INTERVIEW_QUESTION_GENERATION_FAILED, "AI 返回为空");
    }
    String trimmed = content.trim();
    if (trimmed.startsWith("```")) {
      int firstNl = trimmed.indexOf('\n');
      int lastFence = trimmed.lastIndexOf("```");
      if (firstNl > 0 && lastFence > firstNl) {
        trimmed = trimmed.substring(firstNl + 1, lastFence).trim();
      }
    }
    int start = trimmed.indexOf('[');
    int end = trimmed.lastIndexOf(']');
    if (start >= 0 && end > start) {
      return trimmed.substring(start, end + 1);
    }
    throw new BusinessException(ErrorCode.INTERVIEW_QUESTION_GENERATION_FAILED, "无法解析题目 JSON");
  }

  private String nullToEmpty(String s) {
    return s == null ? "" : s;
  }

  private String truncate(String text, int max) {
    if (text == null) {
      return "";
    }
    return text.length() <= max ? text : text.substring(0, max);
  }

  private record GeneratedQuestion(String question, String referenceAnswer) {}
}
