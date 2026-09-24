package interview.guide.modules.auth.service;

import interview.guide.common.config.AuthProperties;
import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import interview.guide.infrastructure.redis.RedisService;
import interview.guide.modules.admin.model.AdminEntity;
import interview.guide.modules.admin.model.CandidateEntity;
import interview.guide.modules.admin.repository.AdminRepository;
import interview.guide.modules.admin.repository.CandidateRepository;
import interview.guide.modules.auth.model.AdminLoginRequest;
import interview.guide.modules.auth.model.AuthSession;
import interview.guide.modules.auth.model.CandidateLoginRequest;
import interview.guide.modules.auth.model.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("鉴权服务")
class AuthServiceTest {

  @Mock
  private AdminRepository adminRepository;
  @Mock
  private CandidateRepository candidateRepository;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private TokenService tokenService;

  @InjectMocks
  private AuthService authService;

  @Nested
  @DisplayName("管理员登录失败")
  class AdminLoginFail {

    @Test
    @DisplayName("用户名不存在抛 AUTH_FAILED")
    void unknownUser() {
      when(adminRepository.findByUsername("nobody")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> authService.adminLogin(new AdminLoginRequest("nobody", "x")))
          .isInstanceOf(BusinessException.class)
          .extracting(ex -> ((BusinessException) ex).getCode())
          .isEqualTo(ErrorCode.AUTH_FAILED.getCode());
      verify(tokenService, never()).createToken(any());
    }

    @Test
    @DisplayName("密码错误抛 AUTH_FAILED")
    void wrongPassword() {
      AdminEntity admin = AdminEntity.builder()
          .id(1L)
          .username("admin")
          .password("encoded")
          .realName("管理员")
          .build();
      when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
      when(passwordEncoder.matches("bad", "encoded")).thenReturn(false);

      assertThatThrownBy(() -> authService.adminLogin(new AdminLoginRequest("admin", "bad")))
          .isInstanceOf(BusinessException.class)
          .extracting(ex -> ((BusinessException) ex).getCode())
          .isEqualTo(ErrorCode.AUTH_FAILED.getCode());
      verify(tokenService, never()).createToken(any());
    }
  }

  @Nested
  @DisplayName("候选人登录失败")
  class CandidateLoginFail {

    @Test
    @DisplayName("手机号不存在抛 AUTH_FAILED")
    void unknownMobile() {
      when(candidateRepository.findByMobile("13900000000")).thenReturn(Optional.empty());

      assertThatThrownBy(() ->
              authService.candidateLogin(new CandidateLoginRequest("13900000000", "x")))
          .isInstanceOf(BusinessException.class)
          .extracting(ex -> ((BusinessException) ex).getCode())
          .isEqualTo(ErrorCode.AUTH_FAILED.getCode());
    }

    @Test
    @DisplayName("密码错误抛 AUTH_FAILED")
    void wrongPassword() {
      CandidateEntity c = CandidateEntity.builder()
          .id(2L)
          .mobile("13800000000")
          .password("encoded")
          .realName("张三")
          .build();
      when(candidateRepository.findByMobile("13800000000")).thenReturn(Optional.of(c));
      when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

      assertThatThrownBy(() ->
              authService.candidateLogin(new CandidateLoginRequest("13800000000", "wrong")))
          .isInstanceOf(BusinessException.class)
          .extracting(ex -> ((BusinessException) ex).getCode())
          .isEqualTo(ErrorCode.AUTH_FAILED.getCode());
    }
  }

  @Nested
  @DisplayName("Token 角色与失效")
  class TokenChecks {

    @Mock
    private RedisService redisService;
    @Mock
    private AuthProperties authProperties;

    @Test
    @DisplayName("候选人访问管理端角色校验失败")
    void roleMismatch() {
      TokenService svc = new TokenService(redisService, authProperties);
      AuthSession session = new AuthSession(1L, UserRole.CANDIDATE, "张三", "13800000000");

      assertThatThrownBy(() -> svc.requireRole(session, UserRole.ADMIN))
          .isInstanceOf(BusinessException.class)
          .satisfies(ex -> assertThat(((BusinessException) ex).getCode())
              .isEqualTo(ErrorCode.FORBIDDEN.getCode()));
    }

    @Test
    @DisplayName("空 token 抛 TOKEN_INVALID")
    void blankToken() {
      TokenService svc = new TokenService(redisService, authProperties);
      assertThatThrownBy(() -> svc.getSession(" "))
          .isInstanceOf(BusinessException.class)
          .satisfies(ex -> assertThat(((BusinessException) ex).getCode())
              .isEqualTo(ErrorCode.TOKEN_INVALID.getCode()));
    }

    @Test
    @DisplayName("Redis 无会话抛 TOKEN_INVALID")
    void missingSession() {
      when(redisService.get(anyString())).thenReturn(null);
      TokenService svc = new TokenService(redisService, authProperties);
      assertThatThrownBy(() -> svc.getSession("dead-token"))
          .isInstanceOf(BusinessException.class)
          .satisfies(ex -> assertThat(((BusinessException) ex).getCode())
              .isEqualTo(ErrorCode.TOKEN_INVALID.getCode()));
    }
  }
}
