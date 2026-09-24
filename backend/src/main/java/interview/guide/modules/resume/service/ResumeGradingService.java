package interview.guide.modules.resume.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import interview.guide.modules.resume.model.ResumeDtos.AnalyzeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeGradingService {

  private final ChatClient chatClient;
  private final ObjectMapper objectMapper;

  public AnalyzeResult analyzeResume(String resumeText) {
    if (resumeText == null || resumeText.isBlank()) {
      throw new BusinessException(ErrorCode.RESUME_ANALYSIS_FAILED, "简历内容为空");
    }

    try {
      String systemPrompt = loadPrompt();
      String userPrompt = "请分析以下简历并按 JSON 格式返回评分结果：\n\n" + truncate(resumeText, 12000);

      String content = chatClient.prompt()
          .system(systemPrompt)
          .user(userPrompt)
          .call()
          .content();

      return parseResult(content);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("简历 AI 评分失败: {}", e.getMessage(), e);
      throw new BusinessException(ErrorCode.RESUME_ANALYSIS_FAILED, "AI 评分失败: " + e.getMessage(), e);
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

  private AnalyzeResult parseResult(String content) throws Exception {
    String json = extractJson(content);
    JsonNode node = objectMapper.readTree(json);
    int score = node.path("overallScore").asInt(60);
    String summary = node.path("summary").asText("暂无摘要");
    List<String> strengths = readStringList(node.get("strengths"));
    List<String> suggestions = readStringList(node.get("suggestions"));
    return new AnalyzeResult(score, summary, strengths, suggestions);
  }

  private List<String> readStringList(JsonNode node) {
    List<String> list = new ArrayList<>();
    if (node != null && node.isArray()) {
      node.forEach(n -> list.add(n.asText()));
    }
    return list;
  }

  private String extractJson(String content) {
    if (content == null) {
      throw new BusinessException(ErrorCode.RESUME_ANALYSIS_FAILED, "AI 返回为空");
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
    throw new BusinessException(ErrorCode.RESUME_ANALYSIS_FAILED, "无法解析 AI 返回 JSON");
  }

  private String loadPrompt() {
    try {
      ClassPathResource resource = new ClassPathResource("prompts/resume-analysis.st");
      return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    } catch (Exception e) {
      return """
          你是资深技术招聘官。请对候选人简历打分，严格输出 JSON：
          {"overallScore":0-100整数,"summary":"一句话总结","strengths":["优点1"],"suggestions":["建议1"]}
          不要输出其他文字。
          """;
    }
  }

  private String truncate(String text, int max) {
    if (text.length() <= max) {
      return text;
    }
    return text.substring(0, max);
  }
}
