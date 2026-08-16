package com.stu.edu.vn.backend.admin.collaborator.identity;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminSocialIdentityRepository extends JpaRepository<AdminSocialIdentity, Long> {
    Optional<AdminSocialIdentity> findByAdminId(Long adminId);
    boolean existsByAdminId(Long adminId);
    boolean existsBySocialUserId(Long socialUserId);
}
