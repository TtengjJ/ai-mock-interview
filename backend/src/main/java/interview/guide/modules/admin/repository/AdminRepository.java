package interview.guide.modules.admin.repository;

import interview.guide.modules.admin.model.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<AdminEntity, Long> {
  Optional<AdminEntity> findByUsername(String username);
  boolean existsByUsername(String username);
}
