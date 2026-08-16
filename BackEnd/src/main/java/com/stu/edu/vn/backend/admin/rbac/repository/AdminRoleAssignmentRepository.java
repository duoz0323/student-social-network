package com.stu.edu.vn.backend.admin.rbac.repository;

import com.stu.edu.vn.backend.admin.rbac.entity.AdminRoleAssignment;
import com.stu.edu.vn.backend.admin.rbac.entity.AdminRoleAssignmentId;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminRoleAssignmentRepository
        extends JpaRepository<AdminRoleAssignment, AdminRoleAssignmentId> {

    long countByIdAdminId(Long adminId);

    @Query(value = """
            SELECT r.code
            FROM roles r JOIN admin_roles ar ON ar.role_id = r.id
            WHERE ar.admin_id = :adminId
            ORDER BY r.id
            """, nativeQuery = true)
    List<String> findRoleCodes(@Param("adminId") Long adminId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ar from AdminRoleAssignment ar where ar.id = :id")
    Optional<AdminRoleAssignment> findByIdForUpdate(@Param("id") AdminRoleAssignmentId id);

    @Query(value = """
            SELECT COUNT(DISTINCT u.id)
            FROM users u
            JOIN admin_roles ar ON ar.admin_id = u.id
            JOIN roles r ON r.id = ar.role_id
            WHERE u.role = 'ADMIN' AND u.status = 'ACTIVE' AND r.code = 'SUPER_ADMIN'
            """, nativeQuery = true)
    long countActiveSuperAdmins();

    @Query(value = "SELECT admin_id FROM admin_roles WHERE role_id = :roleId", nativeQuery = true)
    List<Long> findAdminIdsByRoleId(@Param("roleId") Long roleId);
}
