package com.stu.edu.vn.backend.admin.collaborator.suggestion;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ModerationSuggestionRepository extends JpaRepository<ModerationSuggestion, Long> {
    boolean existsBySuggestedBy_IdAndPost_IdAndStatus(Long adminId, Long postId, ModerationSuggestionStatus status);

    @EntityGraph(attributePaths = {"post", "reviewedBy"})
    @Query("select s from ModerationSuggestion s where s.suggestedBy.id=:adminId and (:status is null or s.status=:status)")
    Page<ModerationSuggestion> findOwn(@Param("adminId") Long adminId,
            @Param("status") ModerationSuggestionStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"post", "suggestedBy", "reviewedBy"})
    @Query("select s from ModerationSuggestion s where (:status is null or s.status=:status)")
    Page<ModerationSuggestion> findForReview(@Param("status") ModerationSuggestionStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"post", "suggestedBy", "reviewedBy"})
    @Query("select s from ModerationSuggestion s where s.id=:id")
    Optional<ModerationSuggestion> findByIdForUpdate(@Param("id") Long id);

    /** Đọc hồ sơ và role của toàn bộ actor trong một lượt để trang danh sách không phát sinh N+1. */
    @Query(value = """
            SELECT u.id AS adminId, up.username AS username, up.display_name AS displayName,
                   up.avatar_url AS avatarUrl,
                   GROUP_CONCAT(r.code ORDER BY r.id SEPARATOR ',') AS roleCodes
            FROM users u
            LEFT JOIN user_profiles up ON up.user_id = u.id
            LEFT JOIN admin_roles ar ON ar.admin_id = u.id
            LEFT JOIN roles r ON r.id = ar.role_id
            WHERE u.id IN (:adminIds)
            GROUP BY u.id, up.username, up.display_name, up.avatar_url
            ORDER BY u.id
            """, nativeQuery = true)
    List<ModerationSuggestionActorProjection> findActorsByAdminIds(
            @Param("adminIds") Collection<Long> adminIds);
}
