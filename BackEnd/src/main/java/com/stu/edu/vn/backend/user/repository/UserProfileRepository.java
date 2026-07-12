package com.stu.edu.vn.backend.user.repository;

import com.stu.edu.vn.backend.user.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository quản lý hồ sơ 1-1 của người dùng.
 */
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * Tìm hồ sơ công khai theo một phần tên hiển thị, chỉ lấy tài khoản ACTIVE đã hoàn tất onboarding.
     */
    @Query(
            value = """
                    SELECT up.*
                    FROM user_profiles up
                    JOIN users u ON u.id = up.user_id
                    WHERE u.status = 'ACTIVE'
                      AND up.profile_completed_at IS NOT NULL
                      AND up.display_name IS NOT NULL
                      AND up.display_name LIKE CONCAT('%', :keyword, '%') ESCAPE '='
                    ORDER BY
                      CASE WHEN up.display_name LIKE CONCAT(:keyword, '%') ESCAPE '=' THEN 0 ELSE 1 END,
                      up.user_id DESC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM user_profiles up
                    JOIN users u ON u.id = up.user_id
                    WHERE u.status = 'ACTIVE'
                      AND up.profile_completed_at IS NOT NULL
                      AND up.display_name IS NOT NULL
                      AND up.display_name LIKE CONCAT('%', :keyword, '%') ESCAPE '='
                    """,
            nativeQuery = true
    )
    Page<UserProfile> searchCompletedActiveProfilesByDisplayName(
            @Param("keyword") String escapedKeyword,
            Pageable pageable
    );
}
