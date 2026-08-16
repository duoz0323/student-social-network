package com.stu.edu.vn.backend.admin.collaborator.suggestion;

import jakarta.persistence.LockModeType;
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
}
