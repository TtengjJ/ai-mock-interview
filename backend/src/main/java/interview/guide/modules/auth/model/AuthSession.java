package interview.guide.modules.auth.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * 存入 Redis 的登录态
 */
public record AuthSession(
    Long userId,
    UserRole role,
    String realName,
    String usernameOrMobile
) implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;
}
