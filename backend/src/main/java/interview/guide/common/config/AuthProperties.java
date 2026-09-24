package interview.guide.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {
  /** Token TTL（小时） */
  private long tokenTtlHours = 24;
  private SeedAdmin seedAdmin = new SeedAdmin();

  @Data
  public static class SeedAdmin {
    private String username = "admin";
    private String password = "admin123";
    private String realName = "系统管理员";
  }
}
