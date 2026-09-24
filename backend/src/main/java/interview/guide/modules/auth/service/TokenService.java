package interview.guide.modules.auth.service;

import interview.guide.common.config.AuthProperties;
import interview.guide.common.constant.CommonConstants;
import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import interview.guide.infrastructure.redis.RedisService;
import interview.guide.modules.auth.model.AuthSession;
import interview.guide.modules.auth.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

  private final RedisService redisService;
  private final AuthProperties authProperties;

  public String createToken(AuthSession session) {
    String token = UUID.randomUUID().toString().replace("-", "");
    redisService.set(
        CommonConstants.Auth.TOKEN_KEY_PREFIX + token,
        session,
        Duration.ofHours(authProperties.getTokenTtlHours())
    );
    return token;
  }

  public AuthSession getSession(String token) {
    if (token == null || token.isBlank()) {
      throw new BusinessException(ErrorCode.TOKEN_INVALID);
    }
    AuthSession session = redisService.get(CommonConstants.Auth.TOKEN_KEY_PREFIX + token);
    if (session == null) {
      throw new BusinessException(ErrorCode.TOKEN_INVALID);
    }
    return session;
  }

  public void invalidate(String token) {
    if (token != null && !token.isBlank()) {
      redisService.delete(CommonConstants.Auth.TOKEN_KEY_PREFIX + token);
    }
  }

  public void requireRole(AuthSession session, UserRole role) {
    if (session == null || session.role() != role) {
      throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该资源");
    }
  }
}
