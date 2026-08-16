package com.stu.edu.vn.backend.admin.rbac.repository;

import com.stu.edu.vn.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Truy vấn danh sách admin kèm roles trong một lượt, tránh N+1. */
public interface AdminAccountRepository extends Repository<User, Long> {

    @Query(value = """
            SELECT u.id AS adminId, u.email AS email, up.username AS username,
                   up.display_name AS displayName, u.status AS status,
                   GROUP_CONCAT(r.code ORDER BY r.id SEPARATOR ',') AS roleCodes,
                   u.created_at AS createdAt
            FROM users u
            LEFT JOIN user_profiles up ON up.user_id = u.id
            LEFT JOIN admin_roles ar ON ar.admin_id = u.id
            LEFT JOIN roles r ON r.id = ar.role_id
            WHERE u.role = 'ADMIN'
              AND (:status IS NULL OR u.status = :status)
              AND (:keyword IS NULL OR LOWER(u.email) LIKE CONCAT('%', LOWER(:keyword), '%')
                   OR LOWER(up.display_name) LIKE CONCAT('%', LOWER(:keyword), '%')
                   OR LOWER(up.username) LIKE CONCAT('%', LOWER(:keyword), '%'))
            GROUP BY u.id, u.email, up.username, up.display_name, u.status, u.created_at
            ORDER BY u.created_at DESC, u.id DESC
            """, countQuery = """
            SELECT COUNT(u.id) FROM users u
            LEFT JOIN user_profiles up ON up.user_id = u.id
            WHERE u.role = 'ADMIN'
              AND (:status IS NULL OR u.status = :status)
              AND (:keyword IS NULL OR LOWER(u.email) LIKE CONCAT('%', LOWER(:keyword), '%')
                   OR LOWER(up.display_name) LIKE CONCAT('%', LOWER(:keyword), '%')
                   OR LOWER(up.username) LIKE CONCAT('%', LOWER(:keyword), '%'))
            """, nativeQuery = true)
    Page<AdminAccountListProjection> findAdmins(
            @Param("keyword") String keyword,
            @Param("status") String status,
            Pageable pageable
    );
}
