package interview.guide.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.storage")
public class StorageConfigProperties {
  private String endpoint = "http://localhost:9000";
  private String accessKey = "minioadmin";
  private String secretKey = "minioadmin";
  private String bucket = "interview-guide";
  private String region = "us-east-1";
}
