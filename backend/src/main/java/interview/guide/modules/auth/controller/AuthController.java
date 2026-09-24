package interview.guide.modules.auth.controller;

import interview.guide.common.constant.CommonConstants;
import interview.guide.common.result.Result;
import interview.guide.modules.auth.model.AdminLoginRequest;
import interview.guide.modules.auth.model.AdminLoginResponse;
import interview.guide.modules.auth.model.AuthSession;
import interview.guide.modules.auth.model.CandidateLoginRequest;
import interview.guide.modules.auth.model.CandidateLoginResponse;
import interview.guide.modules.auth.model.MeResponse;
import interview.guide.modules.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/admin/login")
  public Result<AdminLoginResponse> adminLogin(@Valid @RequestBody AdminLoginRequest request) {
    return Result.success(authService.adminLogin(request));
  }

  @PostMapping("/candidate/login")
  public Result<CandidateLoginResponse> candidateLogin(
      @Valid @RequestBody CandidateLoginRequest request) {
    return Result.success(authService.candidateLogin(request));
  }

  @PostMapping("/logout")
  public Result<Void> logout(HttpServletRequest request) {
    authService.logout(extractToken(request));
    return Result.success();
  }

  @GetMapping("/me")
  public Result<MeResponse> me(HttpServletRequest request) {
    AuthSession session = (AuthSession) request.getAttribute(CommonConstants.Auth.ATTR_USER_CONTEXT);
    return Result.success(authService.me(session));
  }

  private String extractToken(HttpServletRequest request) {
    String header = request.getHeader(CommonConstants.Auth.TOKEN_HEADER);
    if (header != null && header.startsWith(CommonConstants.Auth.BEARER_PREFIX)) {
      return header.substring(CommonConstants.Auth.BEARER_PREFIX.length()).trim();
    }
    return null;
  }
}
