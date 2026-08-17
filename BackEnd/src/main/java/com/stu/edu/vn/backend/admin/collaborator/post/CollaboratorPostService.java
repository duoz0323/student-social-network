package com.stu.edu.vn.backend.admin.collaborator.post;

import com.stu.edu.vn.backend.admin.collaborator.identity.CollaboratorSocialIdentityResolver;
import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.interaction.dto.request.CreateCommentRequest;
import com.stu.edu.vn.backend.interaction.dto.response.CommentResponse;
import com.stu.edu.vn.backend.interaction.service.CommentService;
import com.stu.edu.vn.backend.post.dto.request.CreatePostRequest;
import com.stu.edu.vn.backend.post.dto.request.UpdatePostRequest;
import com.stu.edu.vn.backend.post.dto.response.*;
import com.stu.edu.vn.backend.post.service.PostLikeService;
import com.stu.edu.vn.backend.post.service.PostRepostService;
import com.stu.edu.vn.backend.post.service.PostService;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Wrapper mỏng: resolve actor an toàn rồi gọi đúng Social service đang dùng cho USER. */
@Service
@RequiredArgsConstructor
public class CollaboratorPostService {
    private final CollaboratorSocialIdentityResolver identityResolver;
    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final AdminActionRepository actionRepository;
    private final PostService postService;
    private final PostLikeService likeService;
    private final CommentService commentService;
    private final PostRepostService repostService;

    public PostResponse create(CreatePostRequest request) {
        Actor actor = actor();
        PostResponse response = postService.createPostAs(actor.socialUser().getId(), request);
        audit(actor, AdminActionType.COLLABORATOR_POST_CREATED, response.id());
        return response;
    }

    public OwnedPostDetailResponse detail(Long postId) {
        return postService.getOwnedPostDetailAs(actor().socialUser().getId(), postId);
    }

    public PageResponse<CommentResponse> comments(Long postId, int page, int size) {
        Actor actor = actor();
        // Kiểm tra quyền sở hữu trước khi dùng policy hiển thị bình luận chung của Social User.
        postService.getOwnedPostDetailAs(actor.socialUser().getId(), postId);
        return commentService.getPublishedCommentsAs(actor.socialUser().getId(), postId, page, size);
    }

    public PageResponse<CommentResponse> replies(Long postId, Long commentId, int page, int size) {
        Actor actor = actor();
        // Bài viết và bình luận cha đều phải thuộc đúng hội thoại mà cộng tác viên đang xem.
        postService.getOwnedPostDetailAs(actor.socialUser().getId(), postId);
        return commentService.getPublishedRepliesForPostAs(
                actor.socialUser().getId(), postId, commentId, page, size);
    }

    public PostDetailResponse update(Long postId, UpdatePostRequest request) {
        Actor actor = actor();
        PostDetailResponse response = postService.updatePostAs(actor.socialUser().getId(), postId, request);
        audit(actor, AdminActionType.COLLABORATOR_POST_UPDATED, postId);
        return response;
    }

    public DeletePostResponse delete(Long postId) {
        Actor actor = actor();
        DeletePostResponse response = postService.deletePostAs(actor.socialUser().getId(), postId);
        audit(actor, AdminActionType.COLLABORATOR_POST_DELETED, postId);
        return response;
    }

    public PostLikeResponse like(Long postId) {
        Actor actor = actor();
        return likeService.likePostAs(actor.socialUser().getId(), postId);
    }

    public PostLikeResponse unlike(Long postId) {
        Actor actor = actor();
        return likeService.unlikePostAs(actor.socialUser().getId(), postId);
    }

    public CommentResponse comment(Long postId, CreateCommentRequest request) {
        Actor actor = actor();
        return commentService.createCommentAs(actor.socialUser().getId(), postId, request);
    }

    public CommentResponse reply(Long commentId, CreateCommentRequest request) {
        Actor actor = actor();
        return commentService.createReplyAs(actor.socialUser().getId(), commentId, request);
    }

    public PostRepostResponse repost(Long postId) {
        Actor actor = actor();
        return repostService.repostAs(actor.socialUser().getId(), postId);
    }

    public PostRepostResponse unrepost(Long postId) {
        Actor actor = actor();
        return repostService.unrepostAs(actor.socialUser().getId(), postId);
    }

    private Actor actor() {
        Long adminId = currentUserProvider.getCurrentUserId();
        User admin = userRepository.findById(adminId).orElseThrow();
        return new Actor(admin, identityResolver.resolveActive(adminId));
    }

    private void audit(Actor actor, AdminActionType type, Long postId) {
        actionRepository.save(new AdminAction(actor.admin(), type, AdminTargetType.POST, postId,
                "socialUserId=" + actor.socialUser().getId()));
    }

    private record Actor(User admin, User socialUser) { }
}
