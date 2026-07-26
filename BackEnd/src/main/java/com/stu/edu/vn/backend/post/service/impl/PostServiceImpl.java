package com.stu.edu.vn.backend.post.service.impl;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.post.dto.request.CreatePostRequest;
import com.stu.edu.vn.backend.post.dto.request.UpdatePostRequest;
import com.stu.edu.vn.backend.post.dto.response.DeletePostResponse;
import com.stu.edu.vn.backend.post.dto.response.PostDetailResponse;
import com.stu.edu.vn.backend.post.dto.response.PostResponse;
import com.stu.edu.vn.backend.post.entity.Hashtag;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.entity.PostHashtag;
import com.stu.edu.vn.backend.post.entity.PostMedia;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.enums.PostMediaType;
import com.stu.edu.vn.backend.post.mapper.PostMapper;
import com.stu.edu.vn.backend.post.repository.HashtagRepository;
import com.stu.edu.vn.backend.post.repository.PostHashtagRepository;
import com.stu.edu.vn.backend.post.repository.PostMediaRepository;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.post.service.PostService;
import com.stu.edu.vn.backend.post.validation.HashtagNormalizer;
import com.stu.edu.vn.backend.post.validation.PostImageFileValidator;
import com.stu.edu.vn.backend.post.validation.PostValidationSupport;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.storage.CloudinaryStorageService;
import com.stu.edu.vn.backend.storage.CloudinaryUploadResult;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.service.UserRelationshipPolicyService;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * Triển khai API tạo bài: validate trước upload, rồi lưu database trong một transaction ngắn.
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostServiceImpl.class);
    private static final int MAX_VIDEO_DURATION_SECONDS = 180;

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final HashtagRepository hashtagRepository;
    private final PostHashtagRepository postHashtagRepository;
    private final PostValidationSupport postValidationSupport;
    private final PostImageFileValidator postImageFileValidator;
    private final HashtagNormalizer hashtagNormalizer;
    private final CloudinaryStorageService cloudinaryStorageService;
    private final PostMapper postMapper;
    private final TransactionTemplate transactionTemplate;
    private final EntityManager entityManager;
    private final Clock clock;
    private final UserRelationshipPolicyService relationshipPolicyService;

    @Override
    public PostResponse createPost(CreatePostRequest request) {
        Long authorId = currentUserProvider.getCurrentUserId();
        AuthorContext authorContext = ensureAuthorCanCreatePost(authorId);
        CreatePostCommand command = validateRequest(request);

        List<UploadedPostMedia> uploadedMedia = uploadMedia(command.mediaFiles());
        try {
            return transactionTemplate.execute(status -> createPostInDatabase(authorContext, command, uploadedMedia));
        } catch (RuntimeException exception) {
            cleanupUploadedMedia(uploadedMedia);
            throw exception;
        }
    }

    @Override
    public PostDetailResponse getPostDetail(Long postId) {
        Long viewerId = currentUserProvider.getCurrentUserId();
        ensureViewerCanUsePostApi(viewerId);

        // Chỉ truy vấn bài PUBLISHED để HIDDEN/DELETED trả 404 và không lộ nội dung không hợp lệ.
        Post post = postRepository.findDetailHeaderByIdAndStatus(postId, PostStatus.PUBLISHED)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        if (relationshipPolicyService.existsBlockEitherDirection(viewerId, post.getAuthor().getId())) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        // Tác giả bị khóa thì bài viết không được hiển thị; dùng POST_NOT_FOUND để tránh lộ bài của tài khoản không hợp lệ.
        if (post.getAuthor().getStatus() != UserStatus.ACTIVE || post.getAuthorProfile() == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        List<PostMedia> media = postMediaRepository.findByPost_IdOrderByDisplayOrderAsc(post.getId());
        String hashtag = readSingleHashtag(post.getId());
        boolean owner = post.getAuthor().getId().equals(viewerId);

        return postMapper.toDetailResponse(post, post.getAuthorProfile(), media, hashtag, owner);
    }

    @Override
    @Transactional
    public PostDetailResponse updatePost(Long postId, UpdatePostRequest request) {
        Long viewerId = currentUserProvider.getCurrentUserId();
        ensureViewerCanUsePostApi(viewerId);

        // Chỉ lấy bài PUBLISHED để bài HIDDEN/DELETED trả POST_NOT_FOUND và không lộ trạng thái nội bộ.
        Post post = postRepository.findDetailHeaderByIdAndStatus(postId, PostStatus.PUBLISHED)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        ensureCanEditPost(post, viewerId);

        UpdatePostCommand command = validateUpdateRequest(post, request);
        List<UploadedPostMedia> uploadedMedia = uploadMedia(command.newMediaFiles());
        registerStorageCleanup(uploadedMedia, command.removedMedia());

        UpdatedPostData updatedData = updatePostInDatabase(post, command, uploadedMedia);
        boolean owner = post.getAuthor().getId().equals(viewerId);
        return postMapper.toDetailResponse(
                post,
                post.getAuthorProfile(),
                updatedData.media(),
                updatedData.hashtag(),
                owner
        );
    }

    @Override
    @Transactional
    public DeletePostResponse deletePost(Long postId) {
        Long viewerId = currentUserProvider.getCurrentUserId();
        ensureViewerCanUsePostApi(viewerId);

        // Chỉ tìm bài PUBLISHED để bài HIDDEN/DELETED hoặc không tồn tại đều trả POST_NOT_FOUND và không lộ trạng thái nội bộ.
        Post post = postRepository.findDetailHeaderByIdAndStatus(postId, PostStatus.PUBLISHED)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        ensureCanDeletePost(post, viewerId);

        // deletedAt dùng Clock cấu hình theo hệ thống để thống nhất thời gian Việt Nam trong service và test.
        LocalDateTime deletedAt = LocalDateTime.now(clock);
        int updatedRows = postRepository.softDeletePublishedPost(post.getId(), deletedAt);
        if (updatedRows == 0) {
            // Nếu bài vừa bị ẩn/xóa bởi request khác sau bước đọc, vẫn trả POST_NOT_FOUND theo rule tránh lộ trạng thái.
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        // API chỉ xác nhận xóa mềm, không xóa media, hashtag, comment, like, save hoặc file trên storage.
        return new DeletePostResponse(post.getId(), true);
    }

    private CreatePostCommand validateRequest(CreatePostRequest request) {
        List<MultipartFile> mediaFiles = request == null ? null : request.mediaFiles();
        postImageFileValidator.validate(mediaFiles);

        int mediaCount = postImageFileValidator.countValidImageSlots(mediaFiles);
        String content = postValidationSupport.validateForCreate(request == null ? null : request.content(), mediaCount);
        String hashtag = hashtagNormalizer.normalizeOptional(request == null ? null : request.hashtag());
        return new CreatePostCommand(content, hashtag, mediaFiles == null ? List.of() : mediaFiles);
    }

    private AuthorContext ensureAuthorCanCreatePost(Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (author.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }

        UserProfile profile = userProfileRepository.findById(authorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        if (profile.getProfileCompletedAt() == null) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_COMPLETED);
        }
        return new AuthorContext(author, profile);
    }

    private void ensureViewerCanUsePostApi(Long viewerId) {
        User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (viewer.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }

        UserProfile profile = userProfileRepository.findById(viewerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        if (profile.getProfileCompletedAt() == null) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_COMPLETED);
        }
    }

    private void ensureCanEditPost(Post post, Long viewerId) {
        // Chỉ tác giả bài viết được chỉnh sửa; quyền này bắt buộc kiểm tra ở Backend.
        if (!post.getAuthor().getId().equals(viewerId)) {
            throw new BusinessException(ErrorCode.POST_FORBIDDEN);
        }
        if (post.getAuthor().getStatus() != UserStatus.ACTIVE || post.getAuthorProfile() == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        LocalDateTime postedAt = post.getPublishedAt() == null ? post.getCreatedAt() : post.getPublishedAt();
        LocalDateTime editDeadline = postedAt.plusMinutes(15);
        if (LocalDateTime.now(clock).isAfter(editDeadline)) {
            throw new BusinessException(ErrorCode.POST_EDIT_TIME_EXPIRED);
        }
    }

    private void ensureCanDeletePost(Post post, Long viewerId) {
        // Chỉ tác giả được xóa mềm bài viết của chính mình; Backend không tin vào trạng thái nút trên Frontend.
        if (!post.getAuthor().getId().equals(viewerId)) {
            throw new BusinessException(ErrorCode.POST_FORBIDDEN);
        }
        // Nếu tài khoản tác giả không còn ACTIVE hoặc thiếu hồ sơ, coi như bài không khả dụng để tránh lộ nội dung.
        if (post.getAuthor().getStatus() != UserStatus.ACTIVE || post.getAuthorProfile() == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
    }

    private UpdatePostCommand validateUpdateRequest(Post post, UpdatePostRequest request) {
        List<MultipartFile> newMediaFiles = request == null || request.newMediaFiles() == null
                ? List.of()
                : request.newMediaFiles();
        postImageFileValidator.validate(newMediaFiles);

        String content = postValidationSupport.normalizeContent(request == null ? null : request.content());
        boolean hashtagProvided = request != null && request.hashtag() != null;
        String hashtag = hashtagProvided ? hashtagNormalizer.normalizeOptional(request.hashtag()) : null;
        List<PostMedia> currentMedia = postMediaRepository.findByPost_IdOrderByDisplayOrderAsc(post.getId());
        List<PostMedia> keptMedia = resolveKeptMedia(post.getId(), currentMedia, request == null ? null : request.keepMediaIds());
        List<PostMedia> removedMedia = currentMedia.stream()
                .filter(media -> keptMedia.stream().noneMatch(kept -> kept.getId().equals(media.getId())))
                .toList();

        int imageCount = (int) keptMedia.stream().filter(this::isImage).count();
        int videoCount = keptMedia.size() - imageCount;
        for (MultipartFile file : newMediaFiles) {
            if (postImageFileValidator.detectMediaType(file) == PostMediaType.VIDEO) {
                videoCount++;
            } else {
                imageCount++;
            }
        }
        postImageFileValidator.validateComposition(imageCount, videoCount);
        int finalMediaCount = imageCount + videoCount;
        if ((content == null || content.isBlank()) && finalMediaCount == 0) {
            throw new BusinessException(ErrorCode.POST_CONTENT_REQUIRED);
        }
        return new UpdatePostCommand(content, hashtagProvided, hashtag, keptMedia, removedMedia, newMediaFiles);
    }

    private List<PostMedia> resolveKeptMedia(Long postId, List<PostMedia> currentMedia, List<Long> keepMediaIds) {
        // Nếu client không gửi keepMediaIds, mặc định giữ toàn bộ media cũ để sửa nội dung không vô tình xóa file.
        if (keepMediaIds == null) {
            return currentMedia;
        }
        if (keepMediaIds.isEmpty()) {
            return List.of();
        }

        Set<Long> requestedIds = new HashSet<>(keepMediaIds);
        List<PostMedia> keptMedia = postMediaRepository.findByPost_IdAndIdIn(postId, requestedIds)
                .stream()
                .sorted(Comparator.comparing(PostMedia::getDisplayOrder))
                .toList();
        if (keptMedia.size() != requestedIds.size()) {
            throw new BusinessException(ErrorCode.POST_MEDIA_NOT_FOUND);
        }
        return keptMedia;
    }

    private UpdatedPostData updatePostInDatabase(
            Post post,
            UpdatePostCommand command,
            List<UploadedPostMedia> uploadedMedia
    ) {
        post.setContent(command.content());
        post.setEdited(true);

        // Xóa media cũ không còn được giữ, sau đó flush để tránh va chạm unique (post_id, display_order) khi đánh số lại.
        if (!command.removedMedia().isEmpty()) {
            postMediaRepository.deleteAll(command.removedMedia());
            postMediaRepository.flush();
        }

        List<PostMedia> keptMedia = reorderKeptMedia(command.keptMedia());
        savePostMedia(post, uploadedMedia, keptMedia.size());
        String hashtag = updatePostHashtag(post, command.hashtagProvided(), command.hashtag());

        // Bảo đảm updated_at của posts đổi cả khi chỉ sửa hashtag/media, vì hai phần này nằm ở bảng liên quan.
        postRepository.markEdited(post.getId());
        entityManager.flush();
        entityManager.refresh(post);
        return new UpdatedPostData(
                postMediaRepository.findByPost_IdOrderByDisplayOrderAsc(post.getId()),
                hashtag
        );
    }

    private List<PostMedia> reorderKeptMedia(List<PostMedia> keptMedia) {
        for (int index = 0; index < keptMedia.size(); index++) {
            // Ảnh cũ được giữ đứng trước ảnh mới và được đánh lại displayOrder liên tục từ 0.
            keptMedia.get(index).setDisplayOrder(index);
        }
        return postMediaRepository.saveAllAndFlush(keptMedia);
    }

    private List<PostMedia> savePostMedia(Post post, List<UploadedPostMedia> uploadedMedia, int startDisplayOrder) {
        if (uploadedMedia.isEmpty()) {
            return List.of();
        }
        List<PostMedia> media = uploadedMedia.stream()
                .map(item -> toPostMedia(post, item, startDisplayOrder + item.displayOrder()))
                .toList();
        return postMediaRepository.saveAllAndFlush(media);
    }

    private void registerStorageCleanup(List<UploadedPostMedia> uploadedMedia, List<PostMedia> removedMedia) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            // Trường hợp unit test gọi service trực tiếp không qua Spring proxy; runtime thật sẽ có transaction do @Transactional.
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // Chỉ xóa media cũ trên storage sau khi database commit thành công để tránh mất file khi transaction rollback.
                cleanupRemovedMediaFiles(removedMedia);
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    // Nếu transaction rollback, xóa media mới đã upload để không để lại file mồ côi.
                    cleanupUploadedMedia(uploadedMedia);
                }
            }
        });
    }

    private List<UploadedPostMedia> uploadMedia(List<MultipartFile> mediaFiles) {
        List<UploadedPostMedia> uploadedMedia = new ArrayList<>();
        try {
            for (MultipartFile file : mediaFiles) {
                PostMediaType mediaType = postImageFileValidator.detectMediaType(file);
                CloudinaryUploadResult result = mediaType == PostMediaType.VIDEO
                        ? cloudinaryStorageService.uploadPostVideo(file)
                        : cloudinaryStorageService.uploadPostImage(file);
                if (mediaType == PostMediaType.VIDEO
                        && (result.durationSeconds() == null
                        || result.durationSeconds() <= 0
                        || result.durationSeconds() > MAX_VIDEO_DURATION_SECONDS)) {
                    try {
                        cloudinaryStorageService.deletePostMedia(result.publicId(), mediaType);
                    } catch (RuntimeException cleanupException) {
                        LOGGER.warn("Không thể cleanup video không hợp lệ sau khi upload");
                    }
                    throw new BusinessException(ErrorCode.POST_VIDEO_DURATION_EXCEEDED);
                }
                uploadedMedia.add(new UploadedPostMedia(result, mediaType, uploadedMedia.size()));
            }
            return uploadedMedia;
        } catch (RuntimeException exception) {
            cleanupUploadedMedia(uploadedMedia);
            throw exception;
        }
    }

    private PostResponse createPostInDatabase(
            AuthorContext authorContext,
            CreatePostCommand command,
            List<UploadedPostMedia> uploadedMedia
    ) {
        Post post = postRepository.saveAndFlush(new Post(authorContext.author(), command.content()));
        List<PostMedia> media = savePostMedia(post, uploadedMedia);
        savePostHashtag(post, command.hashtag());

        // Refresh để lấy các giá trị do MySQL tự sinh như created_at, updated_at và published_at.
        entityManager.refresh(post);
        return postMapper.toResponse(post, authorContext.profile(), media, command.hashtag());
    }

    private List<PostMedia> savePostMedia(Post post, List<UploadedPostMedia> uploadedMedia) {
        if (uploadedMedia.isEmpty()) {
            return List.of();
        }
        List<PostMedia> media = uploadedMedia.stream()
                .map(item -> toPostMedia(post, item))
                .toList();
        return postMediaRepository.saveAllAndFlush(media);
    }

    private PostMedia toPostMedia(Post post, UploadedPostMedia uploadedMedia) {
        return toPostMedia(post, uploadedMedia, uploadedMedia.displayOrder());
    }

    private PostMedia toPostMedia(Post post, UploadedPostMedia uploadedMedia, int displayOrder) {
        CloudinaryUploadResult result = uploadedMedia.result();
        return new PostMedia(
                post,
                result.url(),
                result.publicId(),
                uploadedMedia.mediaType(),
                result.mimeType(),
                result.fileSize(),
                result.width(),
                result.height(),
                result.durationSeconds(),
                result.thumbnailUrl(),
                displayOrder
        );
    }

    private void savePostHashtag(Post post, String normalizedHashtag) {
        if (normalizedHashtag == null) {
            return;
        }
        Hashtag hashtag = resolveHashtag(normalizedHashtag);
        postHashtagRepository.saveAndFlush(new PostHashtag(post, hashtag));
    }

    private String updatePostHashtag(Post post, boolean hashtagProvided, String normalizedHashtag) {
        List<PostHashtag> currentRelations = postHashtagRepository.findWithHashtagByPostId(post.getId());
        PostHashtag currentRelation = requireSingleRelation(currentRelations);
        String currentName = currentRelation == null ? null : currentRelation.getHashtag().getNormalizedName();

        if (!hashtagProvided || java.util.Objects.equals(currentName, normalizedHashtag)) {
            // Field absent hoặc giá trị chuẩn hóa không đổi đều giữ nguyên quan hệ và không làm trigger chạy thừa.
            return currentName;
        }
        if (currentRelation != null) {
            postHashtagRepository.deleteByPostId(post.getId());
            postHashtagRepository.flush();
        }
        if (normalizedHashtag == null) {
            return null;
        }

        Hashtag hashtag = resolveHashtag(normalizedHashtag);
        postHashtagRepository.saveAndFlush(new PostHashtag(post, hashtag));
        return normalizedHashtag;
    }

    private Hashtag resolveHashtag(String normalizedHashtag) {
        // Upsert chỉ bỏ qua unique normalized_name; các lỗi database khác vẫn được Spring truyền ra ngoài.
        hashtagRepository.insertIfAbsent(normalizedHashtag, normalizedHashtag);
        return hashtagRepository.findByNormalizedName(normalizedHashtag)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR));
    }

    private String readSingleHashtag(Long postId) {
        PostHashtag relation = requireSingleRelation(postHashtagRepository.findWithHashtagByPostId(postId));
        return relation == null ? null : relation.getHashtag().getNormalizedName();
    }

    private PostHashtag requireSingleRelation(List<PostHashtag> relations) {
        if (relations.size() > 1) {
            // Không âm thầm lấy phần tử đầu vì nhiều quan hệ là dữ liệu vi phạm invariant của module.
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
        return relations.isEmpty() ? null : relations.get(0);
    }

    private void cleanupUploadedMedia(List<UploadedPostMedia> uploadedMedia) {
        for (UploadedPostMedia item : uploadedMedia) {
            try {
                cloudinaryStorageService.deletePostMedia(item.result().publicId(), item.mediaType());
            } catch (RuntimeException cleanupException) {
                // Cleanup thất bại chỉ ghi warning, không che lỗi gốc của upload hoặc database.
                LOGGER.warn("Không thể cleanup media bài viết sau khi lưu thất bại");
            }
        }
    }

    private void cleanupRemovedMediaFiles(List<PostMedia> removedMedia) {
        for (PostMedia media : removedMedia) {
            try {
                cloudinaryStorageService.deletePostMedia(media.getStoragePublicId(), mediaTypeOf(media));
            } catch (RuntimeException cleanupException) {
                // Xóa file cũ thất bại không rollback database vì bài viết đã cập nhật thành công; cần theo dõi log để dọn thủ công nếu cần.
                LOGGER.warn("Không thể xóa media cũ của bài viết sau khi cập nhật bài thành công");
            }
        }
    }

    private boolean isImage(PostMedia media) {
        return mediaTypeOf(media) == PostMediaType.IMAGE;
    }

    private PostMediaType mediaTypeOf(PostMedia media) {
        // Dữ liệu cũ trước migration không có media_type đều là ảnh.
        return media.getMediaType() == null ? PostMediaType.IMAGE : media.getMediaType();
    }

    private record CreatePostCommand(
            String content,
            String hashtag,
            List<MultipartFile> mediaFiles
    ) {
    }

    private record UpdatePostCommand(
            String content,
            boolean hashtagProvided,
            String hashtag,
            List<PostMedia> keptMedia,
            List<PostMedia> removedMedia,
            List<MultipartFile> newMediaFiles
    ) {
    }

    private record UpdatedPostData(
            List<PostMedia> media,
            String hashtag
    ) {
    }

    private record AuthorContext(
            User author,
            UserProfile profile
    ) {
    }

    private record UploadedPostMedia(
            CloudinaryUploadResult result,
            PostMediaType mediaType,
            int displayOrder
    ) {
    }
}
