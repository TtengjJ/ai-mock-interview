package interview.guide.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {
  private String allowedOrigins = "http://localhost:5173,http://localhost:5174";
}
