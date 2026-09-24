package interview.guide.modules.auth.service;

import interview.guide.common.exception.BusinessException;
import interview.guide.common.exception.ErrorCode;
import interview.guide.modules.admin.model.AdminEntity;
import interview.guide.modules.admin.model.CandidateEntity;
import interview.guide.modules.admin.repository.AdminRepository;
import interview.guide.modules.admin.repository.CandidateRepository;
import interview.guide.modules.auth.model.AdminLoginRequest;
import interview.guide.modules.auth.model.AdminLoginResponse;
import interview.guide.modules.auth.model.AuthSession;
import interview.guide.modules.auth.model.CandidateLoginRequest;
import interview.guide.modules.auth.model.CandidateLoginResponse;
import interview.guide.modules.auth.model.MeResponse;
import interview.guide.modules.auth.model.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final AdminRepository adminRepository;
  private final CandidateRepository candidateRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;

  public AdminLoginResponse adminLogin(AdminLoginRequest request) {
    AdminEntity admin = adminRepository.findByUsername(request.username())
        .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_FAILED));
    if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
      throw new BusinessException(ErrorCode.AUTH_FAILED);
    }
    String token = tokenService.createToken(new AuthSession(
        admin.getId(), UserRole.ADMIN, admin.getRealName(), admin.getUsername()
    ));
    log.info("Admin login success: username={}", admin.getUsername());
    return new AdminLoginResponse(token, admin.getRealName());
  }

  public CandidateLoginResponse candidateLogin(CandidateLoginRequest request) {
    CandidateEntity candidate = candidateRepository.findByMobile(request.mobile())
        .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_FAILED));
    if (!passwordEncoder.matches(request.password(), candidate.getPassword())) {
      throw new BusinessException(ErrorCode.AUTH_FAILED);
    }
    String token = tokenService.createToken(new AuthSession(
        candidate.getId(), UserRole.CANDIDATE, candidate.getRealName(), candidate.getMobile()
    ));
    log.info("Candidate login success: candidateId={}", candidate.getId());
    return new CandidateLoginResponse(token, candidate.getRealName(), candidate.getId());
  }

  public void logout(String token) {
    tokenService.invalidate(token);
  }

  public MeResponse me(AuthSession session) {
    return new MeResponse(
        session.userId(),
        session.role().name(),
        session.realName(),
        session.usernameOrMobile()
    );
  }
}
