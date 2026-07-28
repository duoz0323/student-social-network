package com.stu.edu.vn.backend.user.repository;

import com.stu.edu.vn.backend.user.entity.UserBlock;
import com.stu.edu.vn.backend.user.entity.UserBlockId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository tập trung mọi phép kiểm tra quan hệ chặn để các module dùng cùng một định nghĩa. */
public interface UserBlockRepository extends JpaRepository<UserBlock, UserBlockId> {
    boolean existsByIdBlockerIdAndIdBlockedId(Long blockerId, Long blockedId);

    @Modifying
    @Query(value = """
            INSERT INTO user_blocks (blocker_id, blocked_id, created_at)
            VALUES (:blockerId, :blockedId, CURRENT_TIMESTAMP(6))
            ON DUPLICATE KEY UPDATE created_at = user_blocks.created_at
            """, nativeQuery = true)
    int insertIfAbsent(@Param("blockerId") Long blockerId, @Param("blockedId") Long blockedId);

    @Modifying
    @Query("""
            DELETE FROM UserBlock blockRelation
            WHERE blockRelation.id.blockerId = :blockerId
              AND blockRelation.id.blockedId = :blockedId
            """)
    int deleteBlock(@Param("blockerId") Long blockerId, @Param("blockedId") Long blockedId);

    @Query("""
            SELECT CASE WHEN COUNT(blockRelation) > 0 THEN TRUE ELSE FALSE END
            FROM UserBlock blockRelation
            WHERE (blockRelation.id.blockerId = :userAId AND blockRelation.id.blockedId = :userBId)
               OR (blockRelation.id.blockerId = :userBId AND blockRelation.id.blockedId = :userAId)
            """)
    boolean existsEitherDirection(@Param("userAId") Long userAId, @Param("userBId") Long userBId);

    @Query(value = """
            SELECT ub.blocked_id AS userId, up.display_name AS displayName,
                   up.avatar_url AS avatarUrl, ub.created_at AS blockedAt
            FROM user_blocks ub
            JOIN user_profiles up ON up.user_id = ub.blocked_id
            WHERE ub.blocker_id = :blockerId
            ORDER BY ub.created_at DESC, ub.blocked_id DESC
            """,
            countQuery = "SELECT COUNT(*) FROM user_blocks WHERE blocker_id = :blockerId",
            nativeQuery = true)
    Page<BlockedUserProjection> findBlockedUsers(@Param("blockerId") Long blockerId, Pageable pageable);
}
