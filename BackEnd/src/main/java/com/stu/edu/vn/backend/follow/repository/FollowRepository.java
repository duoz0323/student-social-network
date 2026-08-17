package com.stu.edu.vn.backend.follow.repository;

import com.stu.edu.vn.backend.follow.entity.Follow;
import com.stu.edu.vn.backend.follow.entity.FollowId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository truy cập quan hệ Follow và trả projection công khai để tránh tải Entity hoặc dữ liệu nhạy cảm.
 */
public interface FollowRepository extends JpaRepository<Follow, FollowId> {

    // Kiểm tra sớm giúp trả lỗi nghiệp vụ rõ ràng trước khi database chặn khóa chính trùng.
    boolean existsByIdFollowerIdAndIdFollowingId(Long followerId, Long followingId);

    /** Kiểm tra một cặp người dùng đang Follow lẫn nhau bằng đúng một truy vấn. */
    @Query("""
            SELECT CASE WHEN COUNT(relation) = 2 THEN TRUE ELSE FALSE END
            FROM Follow relation
            WHERE (relation.id.followerId = :firstUserId AND relation.id.followingId = :secondUserId)
               OR (relation.id.followerId = :secondUserId AND relation.id.followingId = :firstUserId)
            """)
    boolean existsMutualFollow(
            @Param("firstUserId") Long firstUserId,
            @Param("secondUserId") Long secondUserId
    );

    // Hai phép đếm phục vụ phần thống kê trên trang hồ sơ.
    long countByIdFollowingId(Long followingId);

    long countByIdFollowerId(Long followerId);

    // Xóa trực tiếp bằng hai thành phần khóa kép để Unfollow vẫn hoạt động khi target đã bị khóa.
    @Modifying
    @Query("""
            DELETE FROM Follow f
            WHERE f.id.followerId = :followerId
              AND f.id.followingId = :followingId
            """)
    int deleteFollow(
            @Param("followerId") Long followerId,
            @Param("followingId") Long followingId
    );

    // Lấy toàn bộ follower ACTIVE, thời điểm Follow và trạng thái current user trong đúng một query.
    @Query("""
            SELECT follower.id AS userId,
                   profile.displayName AS displayName,
                   profile.avatarUrl AS avatarUrl,
                   profile.bio AS bio,
                   relation.createdAt AS followedAt,
                   CASE WHEN EXISTS (
                       SELECT currentRelation.id
                       FROM Follow currentRelation
                       WHERE currentRelation.id.followerId = :currentUserId
                         AND currentRelation.id.followingId = follower.id
                   ) THEN TRUE ELSE FALSE END AS followedByCurrentUser
            FROM Follow relation
            JOIN relation.follower follower
            JOIN UserProfile profile ON profile.userId = follower.id
            WHERE relation.id.followingId = :userId
              AND follower.status = com.stu.edu.vn.backend.user.enums.UserStatus.ACTIVE
              AND follower.role = com.stu.edu.vn.backend.user.enums.UserRole.USER
              AND NOT EXISTS (
                  SELECT blockRelation.id
                  FROM UserBlock blockRelation
                  WHERE (blockRelation.id.blockerId = :currentUserId
                         AND blockRelation.id.blockedId = follower.id)
                     OR (blockRelation.id.blockerId = follower.id
                         AND blockRelation.id.blockedId = :currentUserId)
              )
            ORDER BY relation.createdAt DESC, relation.id.followerId DESC
            """)
    List<FollowUserProjection> findActiveFollowers(
            @Param("userId") Long userId,
            @Param("currentUserId") Long currentUserId
    );

    // Lấy toàn bộ tài khoản đang được Follow còn ACTIVE và không phát sinh truy vấn exists theo từng phần tử.
    @Query("""
            SELECT following_user.id AS userId,
                   profile.displayName AS displayName,
                   profile.avatarUrl AS avatarUrl,
                   profile.bio AS bio,
                   relation.createdAt AS followedAt,
                   CASE WHEN EXISTS (
                       SELECT currentRelation.id
                       FROM Follow currentRelation
                       WHERE currentRelation.id.followerId = :currentUserId
                         AND currentRelation.id.followingId = following_user.id
                   ) THEN TRUE ELSE FALSE END AS followedByCurrentUser
            FROM Follow relation
            JOIN relation.following following_user
            JOIN UserProfile profile ON profile.userId = following_user.id
            WHERE relation.id.followerId = :userId
              AND following_user.status = com.stu.edu.vn.backend.user.enums.UserStatus.ACTIVE
              AND following_user.role = com.stu.edu.vn.backend.user.enums.UserRole.USER
              AND NOT EXISTS (
                  SELECT blockRelation.id
                  FROM UserBlock blockRelation
                  WHERE (blockRelation.id.blockerId = :currentUserId
                         AND blockRelation.id.blockedId = following_user.id)
                     OR (blockRelation.id.blockerId = following_user.id
                         AND blockRelation.id.blockedId = :currentUserId)
              )
            ORDER BY relation.createdAt DESC, relation.id.followingId DESC
            """)
    List<FollowUserProjection> findActiveFollowing(
            @Param("userId") Long userId,
            @Param("currentUserId") Long currentUserId
    );
}
