package interview.guide.modules.auth.service;

import interview.guide.common.constant.CommonConstants;
import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import interview.guide.modules.auth.model.AuthSession;
import interview.guide.modules.auth.model.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

  private final TokenService tokenService;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      return true;
    }

    String path = request.getRequestURI();
    String token = extractToken(request);

    // 登录接口放行
    if (path.startsWith("/api/auth/admin/login")
        || path.startsWith("/api/auth/candidate/login")) {
      return true;
    }

    // /api/auth/** 其余需要有效 token（logout / me）
    if (path.startsWith("/api/auth/")) {
      AuthSession session = tokenService.getSession(token);
      request.setAttribute(CommonConstants.Auth.ATTR_USER_CONTEXT, session);
      return true;
    }

    if (path.startsWith("/api/admin/")) {
      AuthSession session = tokenService.getSession(token);
      tokenService.requireRole(session, UserRole.ADMIN);
      request.setAttribute(CommonConstants.Auth.ATTR_USER_CONTEXT, session);
      return true;
    }

    if (path.startsWith("/api/resumes") || path.startsWith("/api/interview")) {
      AuthSession session = tokenService.getSession(token);
      tokenService.requireRole(session, UserRole.CANDIDATE);
      request.setAttribute(CommonConstants.Auth.ATTR_USER_CONTEXT, session);
      return true;
    }

    // 其他 /api/** 默认需要登录
    if (path.startsWith("/api/")) {
      if (token == null || token.isBlank()) {
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
      }
      AuthSession session = tokenService.getSession(token);
      request.setAttribute(CommonConstants.Auth.ATTR_USER_CONTEXT, session);
    }
    return true;
  }

  private String extractToken(HttpServletRequest request) {
    String header = request.getHeader(CommonConstants.Auth.TOKEN_HEADER);
    if (header != null && header.startsWith(CommonConstants.Auth.BEARER_PREFIX)) {
      return header.substring(CommonConstants.Auth.BEARER_PREFIX.length()).trim();
    }
    return null;
  }
}
