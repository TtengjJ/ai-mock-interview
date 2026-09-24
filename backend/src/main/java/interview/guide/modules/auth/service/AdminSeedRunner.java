package interview.guide.modules.auth.service;

import interview.guide.common.config.AuthProperties;
import interview.guide.modules.admin.model.AdminEntity;
import interview.guide.modules.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeedRunner implements ApplicationRunner {

  private final AdminRepository adminRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthProperties authProperties;

  @Override
  public void run(ApplicationArguments args) {
    AuthProperties.SeedAdmin seed = authProperties.getSeedAdmin();
    if (adminRepository.existsByUsername(seed.getUsername())) {
      return;
    }
    AdminEntity admin = AdminEntity.builder()
        .username(seed.getUsername())
        .password(passwordEncoder.encode(seed.getPassword()))
        .realName(seed.getRealName())
        .build();
    adminRepository.save(admin);
    log.info("Seeded default admin: username={}", seed.getUsername());
  }
}
