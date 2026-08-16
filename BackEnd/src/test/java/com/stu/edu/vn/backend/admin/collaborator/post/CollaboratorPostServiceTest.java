package com.stu.edu.vn.backend.admin.collaborator.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.stu.edu.vn.backend.admin.collaborator.identity.CollaboratorSocialIdentityResolver;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.interaction.service.CommentService;
import com.stu.edu.vn.backend.post.dto.request.CreatePostRequest;
import com.stu.edu.vn.backend.post.dto.response.PostLikeResponse;
import com.stu.edu.vn.backend.post.dto.response.PostResponse;
import com.stu.edu.vn.backend.post.dto.response.OwnedPostDetailResponse;
import com.stu.edu.vn.backend.post.service.*;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CollaboratorPostServiceTest {
    @Test
    void detailReadsOnlyPostOwnedByResolvedManagedIdentity() {
        CollaboratorSocialIdentityResolver resolver = mock(CollaboratorSocialIdentityResolver.class);
        CurrentUserProvider current = mock(CurrentUserProvider.class);
        UserRepository users = mock(UserRepository.class);
        PostService posts = mock(PostService.class);
        User admin = mock(User.class);
        User managed = mock(User.class);
        OwnedPostDetailResponse expected = mock(OwnedPostDetailResponse.class);
        when(current.getCurrentUserId()).thenReturn(15L);
        when(users.findById(15L)).thenReturn(Optional.of(admin));
        when(resolver.resolveActive(15L)).thenReturn(managed);
        when(managed.getId()).thenReturn(1050L);
        when(posts.getOwnedPostDetailAs(1050L, 99L)).thenReturn(expected);
        CollaboratorPostService service = new CollaboratorPostService(
                resolver, current, users, mock(AdminActionRepository.class), posts,
                mock(PostLikeService.class), mock(CommentService.class), mock(PostRepostService.class));

        assertThat(service.detail(99L)).isSameAs(expected);
        verify(posts).getOwnedPostDetailAs(1050L, 99L);
        verify(posts, never()).getOwnedPostDetailAs(15L, 99L);
    }

    @Test
    void socialOperationsAlwaysUseResolvedManagedUserInsteadOfAdminId() {
        CollaboratorSocialIdentityResolver resolver = mock(CollaboratorSocialIdentityResolver.class);
        CurrentUserProvider current = mock(CurrentUserProvider.class);
        UserRepository users = mock(UserRepository.class);
        AdminActionRepository actions = mock(AdminActionRepository.class);
        PostService posts = mock(PostService.class);
        PostLikeService likes = mock(PostLikeService.class);
        CommentService comments = mock(CommentService.class);
        PostRepostService reposts = mock(PostRepostService.class);
        User admin = mock(User.class);
        User managed = mock(User.class);
        when(current.getCurrentUserId()).thenReturn(15L);
        when(users.findById(15L)).thenReturn(Optional.of(admin));
        when(resolver.resolveActive(15L)).thenReturn(managed);
        when(managed.getId()).thenReturn(1050L);
        when(likes.likePostAs(1050L, 99L)).thenReturn(new PostLikeResponse(99L, true, 1));
        CollaboratorPostService service = new CollaboratorPostService(
                resolver, current, users, actions, posts, likes, comments, reposts);

        PostLikeResponse response = service.like(99L);

        assertThat(response.likedByCurrentUser()).isTrue();
        verify(likes).likePostAs(1050L, 99L);
        verify(likes, never()).likePostAs(15L, 99L);
    }

    @Test
    void createDelegatesValidationAndMediaFlowToExistingPostService() {
        CollaboratorSocialIdentityResolver resolver = mock(CollaboratorSocialIdentityResolver.class);
        CurrentUserProvider current = mock(CurrentUserProvider.class);
        UserRepository users = mock(UserRepository.class);
        AdminActionRepository actions = mock(AdminActionRepository.class);
        PostService posts = mock(PostService.class);
        User admin = mock(User.class);
        User managed = mock(User.class);
        when(current.getCurrentUserId()).thenReturn(15L);
        when(users.findById(15L)).thenReturn(Optional.of(admin));
        when(resolver.resolveActive(15L)).thenReturn(managed);
        when(managed.getId()).thenReturn(1050L);
        CreatePostRequest request = new CreatePostRequest("Nội dung", "sinhvien", null);
        PostResponse created = mock(PostResponse.class);
        when(created.id()).thenReturn(123L);
        when(posts.createPostAs(1050L, request)).thenReturn(created);
        CollaboratorPostService service = new CollaboratorPostService(resolver, current, users, actions, posts,
                mock(PostLikeService.class), mock(CommentService.class), mock(PostRepostService.class));

        assertThat(service.create(request)).isSameAs(created);
        verify(posts).createPostAs(1050L, request);
        verify(actions).save(argThat(action -> action.getTargetId().equals(123L)
                && action.getNote().contains("socialUserId=1050")));
    }
}
