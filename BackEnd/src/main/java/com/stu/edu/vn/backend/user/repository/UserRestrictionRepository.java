package com.stu.edu.vn.backend.user.repository;

import com.stu.edu.vn.backend.user.entity.UserRestriction;
import com.stu.edu.vn.backend.user.entity.UserRestrictionId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Truy vấn cặp Restrict trực tiếp, tránh tải toàn bộ danh sách quan hệ. */
public interface UserRestrictionRepository extends JpaRepository<UserRestriction, UserRestrictionId> {
    boolean existsByIdRestrictorIdAndIdRestrictedId(Long restrictorId, Long restrictedId);

    @Modifying
    @Query(value = """
            INSERT INTO user_restrictions (restrictor_id, restricted_id, created_at)
            VALUES (:restrictorId, :restrictedId, CURRENT_TIMESTAMP(6))
            ON DUPLICATE KEY UPDATE created_at = user_restrictions.created_at
            """, nativeQuery = true)
    int insertIfAbsent(@Param("restrictorId") Long restrictorId, @Param("restrictedId") Long restrictedId);

    @Modifying
    @Query("""
            DELETE FROM UserRestriction relation
            WHERE relation.id.restrictorId = :restrictorId
              AND relation.id.restrictedId = :restrictedId
            """)
    int deleteRestriction(@Param("restrictorId") Long restrictorId, @Param("restrictedId") Long restrictedId);

    @Query(value = """
            SELECT ur.restricted_id AS userId, up.display_name AS displayName,
                   up.avatar_url AS avatarUrl, ur.created_at AS restrictedAt
            FROM user_restrictions ur
            JOIN users u ON u.id = ur.restricted_id AND u.status = 'ACTIVE'
            JOIN user_profiles up ON up.user_id = ur.restricted_id
            WHERE ur.restrictor_id = :restrictorId
            ORDER BY ur.created_at DESC, ur.restricted_id DESC
            """, countQuery = """
            SELECT COUNT(*) FROM user_restrictions ur
            JOIN users u ON u.id = ur.restricted_id AND u.status = 'ACTIVE'
            WHERE ur.restrictor_id = :restrictorId
            """, nativeQuery = true)
    Page<RestrictedUserProjection> findRestrictedUsers(
            @Param("restrictorId") Long restrictorId, Pageable pageable);
}
