package interview.guide.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "app.resume")
public class ResumeProperties {
  private long maxFileSizeMb = 10;
  private List<String> allowedTypes = new ArrayList<>(List.of(
      "application/pdf",
      "application/msword",
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      "text/plain"
  ));
  private List<String> allowedExtensions = new ArrayList<>(List.of("pdf", "doc", "docx", "txt"));
}
