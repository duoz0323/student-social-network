package com.stu.edu.vn.backend.search.repository;

import com.stu.edu.vn.backend.user.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository truy vấn tìm kiếm, tách khỏi repository hồ sơ thuộc contract Auth/Onboarding. */
public interface SearchUserProfileRepository extends JpaRepository<UserProfile, Long> {

    /** Chỉ trả hồ sơ ACTIVE đã hoàn tất onboarding và ưu tiên tên bắt đầu bằng từ khóa. */
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
