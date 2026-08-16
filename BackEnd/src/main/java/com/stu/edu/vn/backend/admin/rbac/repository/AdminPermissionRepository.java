package com.stu.edu.vn.backend.admin.rbac.repository;

import com.stu.edu.vn.backend.admin.rbac.entity.AdminPermissionEntity;
import java.util.List;
import java.util.Collection;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminPermissionRepository extends JpaRepository<AdminPermissionEntity, Long> {

    List<AdminPermissionEntity> findAllByOrderByCodeAsc();

    List<AdminPermissionEntity> findAllByCodeIn(Collection<String> codes);

    @Query(value = """
            SELECT DISTINCT p.code
            FROM permissions p
            JOIN role_permissions rp ON rp.permission_id = p.id
            JOIN admin_roles ar ON ar.role_id = rp.role_id
            WHERE ar.admin_id = :adminId
            ORDER BY p.code
            """, nativeQuery = true)
    List<String> findEffectiveCodes(@Param("adminId") Long adminId);

    @Query("select p.code from AdminPermissionEntity p order by p.code")
    List<String> findAllCodes();

    @Query(value = """
            SELECT p.code
            FROM permissions p
            JOIN role_permissions rp ON rp.permission_id = p.id
            WHERE rp.role_id = :roleId
            ORDER BY p.code
            """, nativeQuery = true)
    List<String> findCodesByRoleId(@Param("roleId") Long roleId);

    @Modifying(flushAutomatically = true)
    @Query(value = "DELETE FROM role_permissions WHERE role_id = :roleId", nativeQuery = true)
    int deleteMappingsByRoleId(@Param("roleId") Long roleId);

    @Modifying(flushAutomatically = true)
    @Query(value = """
            INSERT INTO role_permissions (role_id, permission_id)
            SELECT :roleId, p.id FROM permissions p WHERE p.code IN (:codes)
            """, nativeQuery = true)
    int insertMappings(@Param("roleId") Long roleId, @Param("codes") Collection<String> codes);
}
