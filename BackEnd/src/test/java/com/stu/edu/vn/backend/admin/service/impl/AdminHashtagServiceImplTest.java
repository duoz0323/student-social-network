package com.stu.edu.vn.backend.admin.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.admin.repository.AdminHashtagRepository;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.repository.projection.AdminHashtagListProjection;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.post.entity.Hashtag;
import com.stu.edu.vn.backend.post.validation.HashtagNormalizer;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.ArgumentCaptor;

class AdminHashtagServiceImplTest {
    private final AdminHashtagRepository repository = mock(AdminHashtagRepository.class);
    private final AdminActionRepository actionRepository = mock(AdminActionRepository.class);
    private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
    private final EntityManager entityManager = mock(EntityManager.class);
    private final AdminHashtagServiceImpl service = new AdminHashtagServiceImpl(
            repository, actionRepository, new HashtagNormalizer(), currentUserProvider, entityManager);

    AdminHashtagServiceImplTest() {
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CustomUserPrincipal(1L, UserRole.ADMIN, UserStatus.ACTIVE));
        when(entityManager.getReference(User.class, 1L)).thenReturn(mock(User.class));
    }

    @Test
    void getHashtagsNormalizesKeywordAndMapsAggregateProjection() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 1, 8, 0);
        LocalDateTime latestUsedAt = LocalDateTime.of(2026, 8, 9, 10, 30);
        AdminHashtagListProjection projection = mock(AdminHashtagListProjection.class);
        when(projection.getHashtagId()).thenReturn(7L);
        when(projection.getName()).thenReturn("Sinh viên");
        when(projection.getPostCount()).thenReturn(12);
        when(projection.getCreatedAt()).thenReturn(createdAt);
        when(projection.getLatestUsedAt()).thenReturn(latestUsedAt);
        when(repository.findAdminHashtags("50=%=_off", PageRequest.of(1, 20)))
                .thenReturn(new PageImpl<>(List.of(projection), PageRequest.of(1, 20), 40));

        var response = service.getHashtags("  50%_off  ", 1, 20);

        assertThat(response.totalElements()).isEqualTo(40);
        assertThat(response.content()).singleElement().satisfies(item -> {
            assertThat(item.hashtagId()).isEqualTo(7L);
            assertThat(item.name()).isEqualTo("Sinh viên");
            assertThat(item.postCount()).isEqualTo(12);
            assertThat(item.createdAt()).isEqualTo(createdAt);
            assertThat(item.latestUsedAt()).isEqualTo(latestUsedAt);
        });
        verify(repository).findAdminHashtags("50=%=_off", PageRequest.of(1, 20));
    }

    @Test
    void getHashtagsUsesNullForBlankKeywordAndKeepsUnusedDateNull() {
        AdminHashtagListProjection projection = mock(AdminHashtagListProjection.class);
        when(projection.getHashtagId()).thenReturn(8L);
        when(projection.getName()).thenReturn("new");
        when(projection.getPostCount()).thenReturn(0);
        when(repository.findAdminHashtags(null, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(projection), PageRequest.of(0, 10), 1));

        var item = service.getHashtags("   ", 0, 10).content().getFirst();

        assertThat(item.postCount()).isZero();
        assertThat(item.latestUsedAt()).isNull();
    }

    @Test
    void getHashtagsRejectsInvalidPaginationAndLongKeyword() {
        assertValidationError(() -> service.getHashtags(null, -1, 20));
        assertValidationError(() -> service.getHashtags(null, 0, 0));
        assertValidationError(() -> service.getHashtags(null, 0, 101));
        assertValidationError(() -> service.getHashtags("a".repeat(101), 0, 20));
    }

    @Test
    void createHashtagNormalizesPersistsAndWritesAudit() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 9, 12, 0);
        when(repository.existsByNormalizedName("sinh viên")).thenReturn(false);
        when(repository.saveAndFlush(any(Hashtag.class))).thenAnswer(invocation -> {
            Hashtag hashtag = invocation.getArgument(0);
            ReflectionTestUtils.setField(hashtag, "id", 15L);
            ReflectionTestUtils.setField(hashtag, "createdAt", createdAt);
            return hashtag;
        });

        var response = service.createHashtag("  ##Sinh   Viên  ");

        assertThat(response.hashtagId()).isEqualTo(15L);
        assertThat(response.name()).isEqualTo("sinh viên");
        assertThat(response.postCount()).isZero();
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.latestUsedAt()).isNull();
        verify(entityManager).refresh(any(Hashtag.class));
        ArgumentCaptor<AdminAction> auditCaptor = ArgumentCaptor.forClass(AdminAction.class);
        verify(actionRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getActionType()).isEqualTo(AdminActionType.CREATE_HASHTAG);
        assertThat(auditCaptor.getValue().getTargetType()).isEqualTo(AdminTargetType.HASHTAG);
        assertThat(auditCaptor.getValue().getTargetId()).isEqualTo(15L);
    }

    @Test
    void createHashtagRejectsBlankInvalidLongAndDuplicateNames() {
        assertError(() -> service.createHashtag("   "), ErrorCode.ADMIN_HASHTAG_NAME_REQUIRED);
        assertError(() -> service.createHashtag("bad-name"), ErrorCode.ADMIN_HASHTAG_NAME_INVALID);
        assertError(() -> service.createHashtag("a".repeat(101)), ErrorCode.ADMIN_HASHTAG_NAME_TOO_LONG);
        when(repository.existsByNormalizedName("existing")).thenReturn(true);
        assertError(() -> service.createHashtag("Existing"), ErrorCode.ADMIN_HASHTAG_ALREADY_EXISTS);
    }

    @Test
    void deleteHashtagDetachesPostsBeforeDeletingAndWritesAudit() {
        Hashtag hashtag = new Hashtag("sinhvien", "sinhvien");
        ReflectionTestUtils.setField(hashtag, "id", 7L);
        when(repository.findByIdForUpdate(7L)).thenReturn(java.util.Optional.of(hashtag));
        when(repository.deletePostRelations(7L)).thenReturn(3);

        var response = service.deleteHashtag(7L);

        assertThat(response.hashtagId()).isEqualTo(7L);
        assertThat(response.name()).isEqualTo("sinhvien");
        assertThat(response.detachedPostCount()).isEqualTo(3);
        var ordered = inOrder(repository);
        ordered.verify(repository).deletePostRelations(7L);
        ordered.verify(repository).delete(hashtag);
        ordered.verify(repository).flush();
        ArgumentCaptor<AdminAction> auditCaptor = ArgumentCaptor.forClass(AdminAction.class);
        verify(actionRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getActionType()).isEqualTo(AdminActionType.DELETE_HASHTAG);
        assertThat(auditCaptor.getValue().getTargetType()).isEqualTo(AdminTargetType.HASHTAG);
        assertThat(auditCaptor.getValue().getNote()).contains("detachedPosts=3");
    }

    @Test
    void deleteHashtagRejectsInvalidOrMissingTarget() {
        assertError(() -> service.deleteHashtag(0L), ErrorCode.VALIDATION_ERROR);
        when(repository.findByIdForUpdate(404L)).thenReturn(java.util.Optional.empty());
        assertError(() -> service.deleteHashtag(404L), ErrorCode.ADMIN_HASHTAG_NOT_FOUND);
    }

    @Test
    void updateHashtagNormalizesNameKeepsEntityAndWritesAudit() {
        Hashtag hashtag = new Hashtag("old", "old");
        ReflectionTestUtils.setField(hashtag, "id", 9L);
        when(repository.findByIdForUpdate(9L)).thenReturn(java.util.Optional.of(hashtag));
        when(repository.existsByNormalizedNameAndIdNot("tên mới", 9L)).thenReturn(false);
        when(repository.saveAndFlush(hashtag)).thenReturn(hashtag);

        var response = service.updateHashtag(9L, " ##Tên   Mới ");

        assertThat(response.hashtagId()).isEqualTo(9L);
        assertThat(response.name()).isEqualTo("tên mới");
        assertThat(hashtag.getNormalizedName()).isEqualTo("tên mới");
        assertThat(hashtag.getDisplayName()).isEqualTo("tên mới");
        ArgumentCaptor<AdminAction> auditCaptor = ArgumentCaptor.forClass(AdminAction.class);
        verify(actionRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getActionType()).isEqualTo(AdminActionType.UPDATE_HASHTAG);
        assertThat(auditCaptor.getValue().getTargetType()).isEqualTo(AdminTargetType.HASHTAG);
        assertThat(auditCaptor.getValue().getNote()).contains("old=#old", "new=#tên mới");
    }

    @Test
    void updateHashtagReturnsNoOpForSameNormalizedName() {
        Hashtag hashtag = new Hashtag("sinh viên", "sinh viên");
        ReflectionTestUtils.setField(hashtag, "id", 9L);
        when(repository.findByIdForUpdate(9L)).thenReturn(java.util.Optional.of(hashtag));

        var response = service.updateHashtag(9L, "##SINH   VIÊN");

        assertThat(response.name()).isEqualTo("sinh viên");
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).saveAndFlush(any(Hashtag.class));
        org.mockito.Mockito.verify(actionRepository, org.mockito.Mockito.never()).save(any(AdminAction.class));
    }

    @Test
    void updateHashtagRejectsMissingDuplicateAndInvalidName() {
        when(repository.findByIdForUpdate(404L)).thenReturn(java.util.Optional.empty());
        assertError(() -> service.updateHashtag(404L, "new"), ErrorCode.ADMIN_HASHTAG_NOT_FOUND);

        Hashtag hashtag = new Hashtag("old", "old");
        ReflectionTestUtils.setField(hashtag, "id", 9L);
        when(repository.findByIdForUpdate(9L)).thenReturn(java.util.Optional.of(hashtag));
        when(repository.existsByNormalizedNameAndIdNot("existing", 9L)).thenReturn(true);
        assertError(() -> service.updateHashtag(9L, "existing"), ErrorCode.ADMIN_HASHTAG_ALREADY_EXISTS);
        assertError(() -> service.updateHashtag(9L, "bad-name"), ErrorCode.ADMIN_HASHTAG_NAME_INVALID);
    }

    private void assertValidationError(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR));
    }

    private void assertError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }
}
