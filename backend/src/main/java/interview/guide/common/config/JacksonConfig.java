package interview.guide.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot 4 默认自动配置 Jackson 3 {@code JsonMapper}，
 * 业务代码仍注入 Jackson 2 {@link ObjectMapper}，需显式注册。
 */
@Configuration
public class JacksonConfig {

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper().findAndRegisterModules();
  }
}
