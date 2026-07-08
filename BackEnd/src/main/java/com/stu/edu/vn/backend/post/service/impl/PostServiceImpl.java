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
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
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
public class PostServiceImpl implements PostService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostServiceImpl.class);

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

    public PostServiceImpl(
            CurrentUserProvider currentUserProvider,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            PostRepository postRepository,
            PostMediaRepository postMediaRepository,
            HashtagRepository hashtagRepository,
            PostHashtagRepository postHashtagRepository,
            PostValidationSupport postValidationSupport,
            PostImageFileValidator postImageFileValidator,
            HashtagNormalizer hashtagNormalizer,
            CloudinaryStorageService cloudinaryStorageService,
            PostMapper postMapper,
            TransactionTemplate transactionTemplate,
            EntityManager entityManager,
            Clock clock
    ) {
        this.currentUserProvider = currentUserProvider;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.postRepository = postRepository;
        this.postMediaRepository = postMediaRepository;
        this.hashtagRepository = hashtagRepository;
        this.postHashtagRepository = postHashtagRepository;
        this.postValidationSupport = postValidationSupport;
        this.postImageFileValidator = postImageFileValidator;
        this.hashtagNormalizer = hashtagNormalizer;
        this.cloudinaryStorageService = cloudinaryStorageService;
        this.postMapper = postMapper;
        this.transactionTemplate = transactionTemplate;
        this.entityManager = entityManager;
        this.clock = clock;
    }

    @Override
    public PostResponse createPost(CreatePostRequest request) {
        CreatePostCommand command = validateRequest(request);
        Long authorId = currentUserProvider.getCurrentUserId();
        AuthorContext authorContext = ensureAuthorCanCreatePost(authorId);

        List<UploadedPostImage> uploadedImages = uploadImages(command.images());
        try {
            return transactionTemplate.execute(status -> createPostInDatabase(authorContext, command, uploadedImages));
        } catch (RuntimeException exception) {
            cleanupUploadedImages(uploadedImages);
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

        // Tác giả bị khóa thì bài viết không được hiển thị; dùng POST_NOT_FOUND để tránh lộ bài của tài khoản không hợp lệ.
        if (post.getAuthor().getStatus() != UserStatus.ACTIVE || post.getAuthorProfile() == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        List<PostMedia> media = postMediaRepository.findByPost_IdOrderByDisplayOrderAsc(post.getId());
        List<String> hashtags = postHashtagRepository.findWithHashtagByPostId(post.getId())
                .stream()
                .map(PostHashtag::getHashtag)
                .map(Hashtag::getNormalizedName)
                .toList();
        boolean owner = post.getAuthor().getId().equals(viewerId);

        return postMapper.toDetailResponse(post, post.getAuthorProfile(), media, hashtags, owner);
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
        List<UploadedPostImage> uploadedImages = uploadImages(command.newImages());
        registerStorageCleanup(uploadedImages, command.removedMedia());

        List<PostMedia> updatedMedia = updatePostInDatabase(post, command, uploadedImages);
        List<String> hashtags = command.hashtags();
        boolean owner = post.getAuthor().getId().equals(viewerId);
        return postMapper.toDetailResponse(post, post.getAuthorProfile(), updatedMedia, hashtags, owner);
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

        // API chỉ xác nhận xóa mềm, không xóa media, hashtag, comment, like, save hoặc file ảnh trên storage.
        return new DeletePostResponse(post.getId(), true);
    }

    private CreatePostCommand validateRequest(CreatePostRequest request) {
        List<MultipartFile> images = request == null ? null : request.images();
        postImageFileValidator.validate(images);

        int imageCount = postImageFileValidator.countValidImageSlots(images);
        String content = postValidationSupport.validateForCreate(request == null ? null : request.content(), imageCount);
        List<String> hashtags = hashtagNormalizer.normalize(request == null ? null : request.hashtags());
        return new CreatePostCommand(content, hashtags, images == null ? List.of() : images);
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
        List<MultipartFile> newImages = request == null || request.newImages() == null
                ? List.of()
                : request.newImages();
        postImageFileValidator.validate(newImages);

        String content = postValidationSupport.normalizeContent(request == null ? null : request.content());
        List<String> hashtags = hashtagNormalizer.normalize(request == null ? null : request.hashtags());
        List<PostMedia> currentMedia = postMediaRepository.findByPost_IdOrderByDisplayOrderAsc(post.getId());
        List<PostMedia> keptMedia = resolveKeptMedia(post.getId(), currentMedia, request == null ? null : request.keepMediaIds());
        List<PostMedia> removedMedia = currentMedia.stream()
                .filter(media -> keptMedia.stream().noneMatch(kept -> kept.getId().equals(media.getId())))
                .toList();

        int finalImageCount = keptMedia.size() + postImageFileValidator.countValidImageSlots(newImages);
        if (finalImageCount > 4) {
            throw new BusinessException(ErrorCode.POST_IMAGE_LIMIT_EXCEEDED);
        }
        if ((content == null || content.isBlank()) && finalImageCount == 0) {
            throw new BusinessException(ErrorCode.POST_CONTENT_REQUIRED);
        }
        return new UpdatePostCommand(content, hashtags, keptMedia, removedMedia, newImages);
    }

    private List<PostMedia> resolveKeptMedia(Long postId, List<PostMedia> currentMedia, List<Long> keepMediaIds) {
        // Nếu client không gửi keepMediaIds, mặc định giữ toàn bộ ảnh cũ để thao tác sửa nội dung không vô tình xóa ảnh.
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

    private List<PostMedia> updatePostInDatabase(
            Post post,
            UpdatePostCommand command,
            List<UploadedPostImage> uploadedImages
    ) {
        post.setContent(command.content());
        post.setEdited(true);

        // Xóa media cũ không còn được giữ, sau đó flush để tránh va chạm unique (post_id, display_order) khi đánh số lại.
        if (!command.removedMedia().isEmpty()) {
            postMediaRepository.deleteAll(command.removedMedia());
            postMediaRepository.flush();
        }

        List<PostMedia> keptMedia = reorderKeptMedia(command.keptMedia());
        savePostMedia(post, uploadedImages, keptMedia.size());
        postHashtagRepository.deleteByPostId(post.getId());
        savePostHashtags(post, command.hashtags());

        // Bảo đảm updated_at của posts đổi cả khi chỉ sửa hashtag/ảnh, vì hai phần này nằm ở bảng liên quan.
        postRepository.markEdited(post.getId());
        entityManager.flush();
        entityManager.refresh(post);
        return postMediaRepository.findByPost_IdOrderByDisplayOrderAsc(post.getId());
    }

    private List<PostMedia> reorderKeptMedia(List<PostMedia> keptMedia) {
        for (int index = 0; index < keptMedia.size(); index++) {
            // Ảnh cũ được giữ đứng trước ảnh mới và được đánh lại displayOrder liên tục từ 0.
            keptMedia.get(index).setDisplayOrder(index);
        }
        return postMediaRepository.saveAllAndFlush(keptMedia);
    }

    private List<PostMedia> savePostMedia(Post post, List<UploadedPostImage> uploadedImages, int startDisplayOrder) {
        if (uploadedImages.isEmpty()) {
            return List.of();
        }
        List<PostMedia> media = uploadedImages.stream()
                .map(uploadedImage -> toPostMedia(post, uploadedImage, startDisplayOrder + uploadedImage.displayOrder()))
                .toList();
        return postMediaRepository.saveAllAndFlush(media);
    }

    private void registerStorageCleanup(List<UploadedPostImage> uploadedImages, List<PostMedia> removedMedia) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            // Trường hợp unit test gọi service trực tiếp không qua Spring proxy; runtime thật sẽ có transaction do @Transactional.
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // Chỉ xóa ảnh cũ trên storage sau khi database commit thành công để tránh mất file khi transaction rollback.
                cleanupRemovedMediaFiles(removedMedia);
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    // Nếu transaction rollback, xóa các ảnh mới đã upload để không để lại file mồ côi.
                    cleanupUploadedImages(uploadedImages);
                }
            }
        });
    }

    private List<UploadedPostImage> uploadImages(List<MultipartFile> images) {
        List<UploadedPostImage> uploadedImages = new ArrayList<>();
        try {
            for (MultipartFile image : images) {
                CloudinaryUploadResult result = cloudinaryStorageService.uploadPostImage(image);
                uploadedImages.add(new UploadedPostImage(result, uploadedImages.size()));
            }
            return uploadedImages;
        } catch (RuntimeException exception) {
            cleanupUploadedImages(uploadedImages);
            throw exception;
        }
    }

    private PostResponse createPostInDatabase(
            AuthorContext authorContext,
            CreatePostCommand command,
            List<UploadedPostImage> uploadedImages
    ) {
        Post post = postRepository.saveAndFlush(new Post(authorContext.author(), command.content()));
        List<PostMedia> media = savePostMedia(post, uploadedImages);
        savePostHashtags(post, command.hashtags());

        // Refresh để lấy các giá trị do MySQL tự sinh như created_at, updated_at và published_at.
        entityManager.refresh(post);
        return postMapper.toResponse(post, authorContext.profile(), media, command.hashtags());
    }

    private List<PostMedia> savePostMedia(Post post, List<UploadedPostImage> uploadedImages) {
        if (uploadedImages.isEmpty()) {
            return List.of();
        }
        List<PostMedia> media = uploadedImages.stream()
                .map(uploadedImage -> toPostMedia(post, uploadedImage))
                .toList();
        return postMediaRepository.saveAllAndFlush(media);
    }

    private PostMedia toPostMedia(Post post, UploadedPostImage uploadedImage) {
        return toPostMedia(post, uploadedImage, uploadedImage.displayOrder());
    }

    private PostMedia toPostMedia(Post post, UploadedPostImage uploadedImage, int displayOrder) {
        CloudinaryUploadResult result = uploadedImage.result();
        PostMedia media = new PostMedia(
                post,
                result.url(),
                result.publicId(),
                result.mimeType(),
                result.fileSize(),
                displayOrder
        );
        media.setWidthPx(result.width());
        media.setHeightPx(result.height());
        return media;
    }

    private void savePostHashtags(Post post, List<String> normalizedHashtags) {
        if (normalizedHashtags.isEmpty()) {
            return;
        }

        Map<String, Hashtag> hashtagsByName = resolveHashtags(normalizedHashtags);
        List<PostHashtag> postHashtags = normalizedHashtags.stream()
                .map(hashtagsByName::get)
                .map(hashtag -> new PostHashtag(post, hashtag))
                .toList();
        postHashtagRepository.saveAllAndFlush(postHashtags);
    }

    private Map<String, Hashtag> resolveHashtags(List<String> normalizedHashtags) {
        for (String normalizedHashtag : normalizedHashtags) {
            // MySQL unique key bảo đảm chỉ một request tạo được hashtag; request còn lại tái sử dụng bản ghi.
            hashtagRepository.insertIfAbsent(normalizedHashtag, normalizedHashtag);
        }

        List<Hashtag> hashtags = hashtagRepository.findByNormalizedNameIn(normalizedHashtags);
        Map<String, Hashtag> hashtagsByName = new LinkedHashMap<>();
        for (Hashtag hashtag : hashtags) {
            hashtagsByName.put(hashtag.getNormalizedName(), hashtag);
        }
        for (String normalizedHashtag : normalizedHashtags) {
            if (!hashtagsByName.containsKey(normalizedHashtag)) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR);
            }
        }
        return hashtagsByName;
    }

    private void cleanupUploadedImages(List<UploadedPostImage> uploadedImages) {
        for (UploadedPostImage uploadedImage : uploadedImages) {
            try {
                cloudinaryStorageService.deleteImage(uploadedImage.result().publicId());
            } catch (RuntimeException cleanupException) {
                // Cleanup thất bại chỉ ghi warning, không che lỗi gốc của upload hoặc database.
                LOGGER.warn("Không thể cleanup ảnh bài viết sau khi tạo bài thất bại");
            }
        }
    }

    private void cleanupRemovedMediaFiles(List<PostMedia> removedMedia) {
        for (PostMedia media : removedMedia) {
            try {
                cloudinaryStorageService.deleteImage(media.getStoragePublicId());
            } catch (RuntimeException cleanupException) {
                // Xóa file cũ thất bại không rollback database vì bài viết đã cập nhật thành công; cần theo dõi log để dọn thủ công nếu cần.
                LOGGER.warn("Không thể xóa ảnh cũ của bài viết sau khi cập nhật bài thành công");
            }
        }
    }

    private record CreatePostCommand(
            String content,
            List<String> hashtags,
            List<MultipartFile> images
    ) {
    }

    private record UpdatePostCommand(
            String content,
            List<String> hashtags,
            List<PostMedia> keptMedia,
            List<PostMedia> removedMedia,
            List<MultipartFile> newImages
    ) {
    }

    private record AuthorContext(
            User author,
            UserProfile profile
    ) {
    }

    private record UploadedPostImage(
            CloudinaryUploadResult result,
            int displayOrder
    ) {
    }
}
