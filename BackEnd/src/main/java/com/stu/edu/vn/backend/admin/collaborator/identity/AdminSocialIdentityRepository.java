package com.stu.edu.vn.backend.admin.collaborator.identity;

import java.util.Optional;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminSocialIdentityRepository extends JpaRepository<AdminSocialIdentity, Long> {
    Optional<AdminSocialIdentity> findByAdminId(Long adminId);
    boolean existsByAdminId(Long adminId);
    boolean existsBySocialUserId(Long socialUserId);

    /** Badge chỉ ACTIVE khi liên kết còn hoạt động và Admin vẫn thực sự có role COLLABORATOR. */
    @Query(value = """
            SELECT DISTINCT identity.social_user_id
            FROM admin_social_identities identity
            JOIN users admin_user ON admin_user.id = identity.admin_id
            JOIN users social_user ON social_user.id = identity.social_user_id
            JOIN admin_roles assignment ON assignment.admin_id = admin_user.id
            JOIN roles role_definition ON role_definition.id = assignment.role_id
            WHERE identity.social_user_id IN (:socialUserIds)
              AND identity.status = 'ACTIVE'
              AND admin_user.role = 'ADMIN' AND admin_user.status = 'ACTIVE'
              AND social_user.role = 'USER' AND social_user.status = 'ACTIVE'
              AND social_user.account_type = 'MANAGED'
              AND role_definition.code = 'COLLABORATOR'
            """, nativeQuery = true)
    List<Long> findActiveCollaboratorSocialUserIds(
            @Param("socialUserIds") Collection<Long> socialUserIds);
}
