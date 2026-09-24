package interview.guide.modules.interview.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import interview.guide.modules.admin.model.InterviewerConfigEntity;
import interview.guide.modules.admin.model.JobEntity;
import interview.guide.modules.admin.repository.InterviewerConfigRepository;
import interview.guide.modules.interview.model.InterviewAnswerEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewEvaluationService {

  private final ChatClient chatClient;
  private final ObjectMapper objectMapper;
  private final InterviewerConfigRepository interviewerConfigRepository;

  public EvaluationResult evaluate(JobEntity job, List<InterviewAnswerEntity> answers) {
    String systemPrompt = resolveSystemPrompt(job);
    StringBuilder qa = new StringBuilder();
    for (InterviewAnswerEntity a : answers) {
      qa.append("【题").append(a.getQuestionIndex() + 1).append("】(").append(a.getSource()).append(")\n");
      qa.append("问：").append(a.getQuestionText()).append('\n');
      qa.append("答：").append(nullToEmpty(a.getAnswerText())).append('\n');
      if (a.getFollowUpQuestion() != null && !a.getFollowUpQuestion().isBlank()) {
        qa.append("追问：").append(a.getFollowUpQuestion()).append('\n');
        qa.append("追问答：").append(nullToEmpty(a.getFollowUpAnswer())).append('\n');
      }
      qa.append('\n');
    }

    try {
      String userPrompt = """
          岗位：%s
          岗位评估提示：%s
          请根据问答记录给出面试评估，严格输出 JSON：
          {"overallScore":0-100整数,"overallFeedback":"总评","strengths":["优点"],"improvements":["改进建议"]}
          问答记录：
          %s
          """.formatted(
          nullToEmpty(job.getJobName()),
          nullToEmpty(job.getPrompt()),
          qa.toString());

      String content = chatClient.prompt()
          .system(systemPrompt)
          .user(userPrompt)
          .call()
          .content();
      return parse(content);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("面试评估失败: jobId={}", job.getId(), e);
      throw new BusinessException(ErrorCode.INTERVIEW_EVALUATION_FAILED, "面试评估失败", e);
    }
  }

  public String toJsonArray(List<String> items) {
    try {
      return objectMapper.writeValueAsString(items == null ? List.of() : items);
    } catch (Exception e) {
      return "[]";
    }
  }

  public List<String> fromJsonArray(String json) {
    if (json == null || json.isBlank()) {
      return List.of();
    }
    try {
      return objectMapper.readValue(json, new TypeReference<List<String>>() {});
    } catch (Exception e) {
      return List.of(json);
    }
  }

  private String resolveSystemPrompt(JobEntity job) {
    if (job.getInterviewerConfigId() != null) {
      return interviewerConfigRepository.findById(job.getInterviewerConfigId())
          .map(InterviewerConfigEntity::getSystemPrompt)
          .filter(p -> p != null && !p.isBlank())
          .orElse(defaultPrompt());
    }
    return defaultPrompt();
  }

  private String defaultPrompt() {
    return "你是资深技术面试官，请客观公正评估候选人表现，输出指定 JSON。";
  }

  private EvaluationResult parse(String content) throws Exception {
    String json = extractJson(content);
    JsonNode node = objectMapper.readTree(json);
    int score = node.path("overallScore").asInt(60);
    String feedback = node.path("overallFeedback").asText("暂无总评");
    List<String> strengths = readList(node.get("strengths"));
    List<String> improvements = readList(node.get("improvements"));
    return new EvaluationResult(score, feedback, strengths, improvements);
  }

  private List<String> readList(JsonNode node) {
    List<String> list = new ArrayList<>();
    if (node != null && node.isArray()) {
      node.forEach(n -> list.add(n.asText()));
    }
    return list;
  }

  private String extractJson(String content) {
    if (content == null) {
      throw new BusinessException(ErrorCode.INTERVIEW_EVALUATION_FAILED, "AI 返回为空");
    }
    String trimmed = content.trim();
    if (trimmed.startsWith("```")) {
      int firstNl = trimmed.indexOf('\n');
      int lastFence = trimmed.lastIndexOf("```");
      if (firstNl > 0 && lastFence > firstNl) {
        trimmed = trimmed.substring(firstNl + 1, lastFence).trim();
      }
    }
    int start = trimmed.indexOf('{');
    int end = trimmed.lastIndexOf('}');
    if (start >= 0 && end > start) {
      return trimmed.substring(start, end + 1);
    }
    throw new BusinessException(ErrorCode.INTERVIEW_EVALUATION_FAILED, "无法解析评估 JSON");
  }

  private String nullToEmpty(String s) {
    return s == null ? "" : s;
  }

  public record EvaluationResult(
      int overallScore,
      String overallFeedback,
      List<String> strengths,
      List<String> improvements
  ) {}
}
