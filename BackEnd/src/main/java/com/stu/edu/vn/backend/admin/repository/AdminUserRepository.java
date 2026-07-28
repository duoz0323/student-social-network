package com.stu.edu.vn.backend.admin.repository;

import com.stu.edu.vn.backend.user.entity.User;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Repository đọc dữ liệu quản trị bằng projection, không tải password_hash hoặc toàn bộ Entity.
 */
public interface AdminUserRepository extends Repository<User, Long> {

    /**
     * Khóa hàng users mục tiêu đến hết transaction để tuần tự hóa các yêu cầu đổi trạng thái đồng thời.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :userId")
    Optional<User> findByIdForUpdate(@Param("userId") Long userId);

    @Query(
            value = """
                    SELECT u.id AS userId,
                           up.display_name AS displayName,
                           up.avatar_url AS avatarUrl,
                           u.email AS email,
                           CAST(NULL AS CHAR) AS phoneNumber,
                           u.status AS status,
                           up.profile_completed_at AS profileCompletedAt,
                           u.created_at AS createdAt
                    FROM users u
                    LEFT JOIN user_profiles up ON up.user_id = u.id
                    WHERE u.role = 'USER'
                      AND (:status IS NULL OR u.status = :status)
                      AND (
                          :keyword IS NULL
                          OR LOWER(u.email) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '='
                          OR LOWER(up.display_name) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '='
                      )
                    ORDER BY u.created_at DESC, u.id DESC
                    """,
            countQuery = """
                    SELECT COUNT(u.id)
                    FROM users u
                    LEFT JOIN user_profiles up ON up.user_id = u.id
                    WHERE u.role = 'USER'
                      AND (:status IS NULL OR u.status = :status)
                      AND (
                          :keyword IS NULL
                          OR LOWER(u.email) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '='
                          OR LOWER(up.display_name) LIKE CONCAT('%', LOWER(:keyword), '%') ESCAPE '='
                      )
                    """,
            nativeQuery = true
    )
    Page<AdminUserListProjection> findManagedUsers(
            @Param("keyword") String escapedKeyword,
            @Param("status") String status,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT u.id AS userId,
                           up.display_name AS displayName,
                           up.avatar_url AS avatarUrl,
                           up.bio AS bio,
                           u.email AS email,
                           CAST(NULL AS CHAR) AS phoneNumber,
                           u.role AS role,
                           u.status AS status,
                           up.profile_completed_at AS profileCompletedAt,
                           u.blocked_at AS blockedAt,
                           u.blocked_reason AS blockedReason,
                           u.created_at AS createdAt,
                           u.updated_at AS updatedAt
                    FROM users u
                    LEFT JOIN user_profiles up ON up.user_id = u.id
                    WHERE u.id = :userId
                    """,
            nativeQuery = true
    )
    Optional<AdminUserDetailProjection> findManagedUserDetail(@Param("userId") Long userId);
}
