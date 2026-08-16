package com.stu.edu.vn.backend.post.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.post.dto.request.CreatePostRequest;
import com.stu.edu.vn.backend.post.dto.request.UpdatePostRequest;
import com.stu.edu.vn.backend.post.dto.request.PostLocationRequest;
import com.stu.edu.vn.backend.post.enums.LocationAction;
import com.stu.edu.vn.backend.location.entity.Location;
import com.stu.edu.vn.backend.moderation.service.ContentModerationService;
import com.stu.edu.vn.backend.post.dto.response.DeletePostResponse;
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
import com.stu.edu.vn.backend.post.repository.PostLikeRepository;
import com.stu.edu.vn.backend.post.repository.PostMediaRepository;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.post.repository.PostRepostRepository;
import com.stu.edu.vn.backend.post.validation.HashtagNormalizer;
import com.stu.edu.vn.backend.post.validation.PostImageFileValidator;
import com.stu.edu.vn.backend.post.validation.PostValidationSupport;
import com.stu.edu.vn.backend.post.validation.PostLocationValidator;
import com.stu.edu.vn.backend.location.service.LocationResolver;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@SuppressWarnings({"unchecked", "rawtypes"})
class PostServiceImplTest {

    private final CurrentUserProvider currentUserProvider = org.mockito.Mockito.mock(CurrentUserProvider.class);
    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final UserProfileRepository userProfileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
    private final PostRepository postRepository = org.mockito.Mockito.mock(PostRepository.class);
    private final PostRepostRepository postRepostRepository = org.mockito.Mockito.mock(PostRepostRepository.class);
    private final PostLikeRepository postLikeRepository = org.mockito.Mockito.mock(PostLikeRepository.class);
    private final PostMediaRepository postMediaRepository = org.mockito.Mockito.mock(PostMediaRepository.class);
    private final HashtagRepository hashtagRepository = org.mockito.Mockito.mock(HashtagRepository.class);
    private final PostHashtagRepository postHashtagRepository = org.mockito.Mockito.mock(PostHashtagRepository.class);
    private final CloudinaryStorageService cloudinaryStorageService = org.mockito.Mockito.mock(CloudinaryStorageService.class);
    private final TransactionTemplate transactionTemplate = org.mockito.Mockito.mock(TransactionTemplate.class);
    private final EntityManager entityManager = org.mockito.Mockito.mock(EntityManager.class);
    private final LocationResolver locationResolver = org.mockito.Mockito.mock(LocationResolver.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-03T01:10:00Z"), ZoneId.of("UTC"));
    private final UserRelationshipPolicyService relationshipPolicyService =
            org.mockito.Mockito.mock(UserRelationshipPolicyService.class);
    private final ContentModerationService contentModerationService =
            org.mockito.Mockito.mock(ContentModerationService.class);

    private final AtomicLong postIds = new AtomicLong(100);
    private final AtomicLong mediaIds = new AtomicLong(200);

    private PostServiceImpl postService;

    @BeforeEach
    void setUp() {
        postService = new PostServiceImpl(
                currentUserProvider,
                userRepository,
                userProfileRepository,
                postRepository,
                postRepostRepository,
                postLikeRepository,
                postMediaRepository,
                hashtagRepository,
                postHashtagRepository,
                new PostValidationSupport(),
                new PostImageFileValidator(),
                new HashtagNormalizer(),
                new PostLocationValidator(),
                locationResolver,
                cloudinaryStorageService,
                Mappers.getMapper(PostMapper.class),
                transactionTemplate,
                entityManager,
                clock,
                relationshipPolicyService,
                contentModerationService
        );

        when(currentUserProvider.getCurrentUserId()).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user(10L)));
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(completedProfile(10L)));
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(new SimpleTransactionStatus());
        });
        when(postRepository.saveAndFlush(any(Post.class))).thenAnswer(invocation -> savedPost(invocation.getArgument(0)));
        when(postRepository.findDetailHeaderByIdAndStatusForUpdate(any(), eq(PostStatus.PUBLISHED)))
                .thenAnswer(invocation -> postRepository.findDetailHeaderByIdAndStatus(
                        invocation.getArgument(0), PostStatus.PUBLISHED));
        when(postMediaRepository.saveAllAndFlush(any())).thenAnswer(invocation -> saveMedia(invocation.getArgument(0)));
        when(postHashtagRepository.saveAndFlush(any(PostHashtag.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(hashtagRepository.findByNormalizedName(any())).thenAnswer(invocation -> {
            String normalizedName = invocation.getArgument(0);
            return Optional.of(hashtag(normalizedName, Math.abs((long) normalizedName.hashCode())));
        });
    }

    @Test
    void createPostWithContentOnlyUsesCurrentUserAndReturnsPostResponse() {
        CreatePostRequest request = new CreatePostRequest("  Xin chao sinh vien  ", null, null);

        PostResponse response = postService.createPost(request);

        verify(currentUserProvider).getCurrentUserId();
        verify(userRepository).findById(10L);
        verify(cloudinaryStorageService, never()).uploadPostImage(any());
        assertThat(response.content()).isEqualTo("Xin chao sinh vien");
        assertThat(response.author().id()).isEqualTo(10L);
        assertThat(response.media()).isEmpty();
        assertThat(response.hashtag()).isNull();
    }

    @Test
    void createPostDoesNotPersistWhenModerationRejectsOrIsUnavailable() {
        for (ErrorCode errorCode : List.of(
                ErrorCode.CONTENT_MODERATION_WARNING,
                ErrorCode.CONTENT_POLICY_VIOLATION,
                ErrorCode.CONTENT_MODERATION_UNAVAILABLE)) {
            org.mockito.Mockito.reset(contentModerationService, transactionTemplate);
            doThrow(new BusinessException(errorCode)).when(contentModerationService).requireAllowed("Nội dung cần kiểm tra");

            assertThatThrownBy(() -> postService.createPost(
                    new CreatePostRequest("Nội dung cần kiểm tra", null, null)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(errorCode);
            verify(transactionTemplate, never()).execute(any());
            verify(postRepository, never()).saveAndFlush(any(Post.class));
        }
    }

    @Test
    void createPostResolvesLocationInsideDatabaseTransactionAndReturnsIt() {
        PostLocationRequest requestLocation = locationRequest("ChIJ-id");
        Location location = location("ChIJ-id", "Đại học STU", 501L);
        when(locationResolver.resolve(any(PostLocationRequest.class))).thenReturn(location);

        PostResponse response = postService.createPost(
                new CreatePostRequest("Nội dung", null, null, requestLocation));

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).saveAndFlush(postCaptor.capture());
        assertThat(postCaptor.getValue().getLocation()).isSameAs(location);
        assertThat(response.location().placeId()).isEqualTo("ChIJ-id");
    }

    @Test
    void createPostWithOneImageAndNoContentStoresImageMetadata() {
        MockMultipartFile image = png("one.png");
        when(cloudinaryStorageService.uploadPostImage(image))
                .thenReturn(upload("https://cdn.example/one.png", "post/one", 640, 480));

        PostResponse response = postService.createPost(new CreatePostRequest(null, null, List.of(image)));

        assertThat(response.content()).isNull();
        assertThat(response.media()).hasSize(1);
        assertThat(response.media().getFirst().url()).isEqualTo("https://cdn.example/one.png");
        assertThat(response.media().getFirst().displayOrder()).isZero();
    }

    @Test
    void createPostWithOneVideoStoresDurationThumbnailAndMediaType() {
        MockMultipartFile video = mp4("intro.mp4");
        when(cloudinaryStorageService.uploadPostVideo(video)).thenReturn(new CloudinaryUploadResult(
                "https://cdn.example/intro.mp4", "post/intro", "video/mp4", 2048L,
                1280, 720, 90, "https://cdn.example/intro.jpg"));

        PostResponse response = postService.createPost(new CreatePostRequest(null, null, List.of(video)));

        assertThat(response.media()).singleElement().satisfies(item -> {
            assertThat(item.mediaType()).isEqualTo(PostMediaType.VIDEO);
            assertThat(item.durationSeconds()).isEqualTo(90);
            assertThat(item.thumbnailUrl()).isEqualTo("https://cdn.example/intro.jpg");
        });
        verify(cloudinaryStorageService).uploadPostVideo(video);
    }

    @Test
    void createPostAcceptsOneVideoAndThreeImagesInOriginalOrder() {
        MockMultipartFile firstImage = png("first.png");
        MockMultipartFile video = mp4("video.mp4");
        MockMultipartFile secondImage = jpeg("second.jpg");
        MockMultipartFile thirdImage = webp("third.webp");
        when(cloudinaryStorageService.uploadPostImage(firstImage))
                .thenReturn(upload("https://cdn.example/first.png", "post/first", 640, 480));
        when(cloudinaryStorageService.uploadPostVideo(video)).thenReturn(new CloudinaryUploadResult(
                "https://cdn.example/video.mp4", "post/video", "video/mp4", 2048L,
                1280, 720, 90, "https://cdn.example/video.jpg"));
        when(cloudinaryStorageService.uploadPostImage(secondImage))
                .thenReturn(upload("https://cdn.example/second.jpg", "post/second", 640, 480));
        when(cloudinaryStorageService.uploadPostImage(thirdImage))
                .thenReturn(upload("https://cdn.example/third.webp", "post/third", 640, 480));

        PostResponse response = postService.createPost(new CreatePostRequest(
                "Noi dung", null, List.of(firstImage, video, secondImage, thirdImage)));

        assertThat(response.media()).extracting("mediaType")
                .containsExactly(PostMediaType.IMAGE, PostMediaType.VIDEO,
                        PostMediaType.IMAGE, PostMediaType.IMAGE);
        assertThat(response.media()).extracting("displayOrder").containsExactly(0, 1, 2, 3);
    }

    @Test
    void createPostRejectsMoreThanFourMixedMediaBeforeUpload() {
        assertThatThrownBy(() -> postService.createPost(new CreatePostRequest(
                "Noi dung", null, List.of(
                        png("1.png"), png("2.png"), png("3.png"), png("4.png"), mp4("video.mp4")
                ))))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_MEDIA_LIMIT_EXCEEDED);

        verify(cloudinaryStorageService, never()).uploadPostImage(any());
        verify(cloudinaryStorageService, never()).uploadPostVideo(any());
    }

    @Test
    void createPostDeletesUploadedVideoWhenDurationExceedsThreeMinutes() {
        MockMultipartFile video = mp4("long.mp4");
        when(cloudinaryStorageService.uploadPostVideo(video)).thenReturn(new CloudinaryUploadResult(
                "https://cdn.example/long.mp4", "post/long", "video/mp4", 2048L,
                1280, 720, 181, "https://cdn.example/long.jpg"));

        assertThatThrownBy(() -> postService.createPost(new CreatePostRequest(null, null, List.of(video))))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_VIDEO_DURATION_EXCEEDED);

        verify(cloudinaryStorageService).deletePostMedia("post/long", PostMediaType.VIDEO);
        verify(transactionTemplate, never()).execute(any());
    }

    @Test
    void createPostWithContentHashtagAndFourImagesKeepsImageOrderAndNormalizedHashtag() {
        List<MultipartFile> images = List.of(png("1.png"), jpeg("2.jpg"), webp("3.webp"), png("4.png"));
        for (int index = 0; index < images.size(); index++) {
            when(cloudinaryStorageService.uploadPostImage(images.get(index)))
                    .thenReturn(upload("https://cdn.example/" + index + ".png", "post/" + index, 100 + index, 200 + index));
        }

        PostResponse response = postService.createPost(new CreatePostRequest(
                "  Noi dung  ",
                "#Sinh   #Viên",
                images
        ));

        assertThat(response.content()).isEqualTo("Noi dung");
        assertThat(response.hashtag()).isEqualTo("sinh viên");
        assertThat(response.media()).extracting("displayOrder").containsExactly(0, 1, 2, 3);
        verify(hashtagRepository).insertIfAbsent("sinh viên", "sinh viên");
    }

    @Test
    void createPostRejectsRequestWithoutContentAndImages() {
        assertThatThrownBy(() -> postService.createPost(new CreatePostRequest("   ", null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_CONTENT_OR_IMAGE_REQUIRED);

        verify(cloudinaryStorageService, never()).uploadPostImage(any());
        verify(transactionTemplate, never()).execute(any());
    }

    @Test
    void createPostRejectsHashtagOnlyBecauseItIsNotPostContent() {
        assertThatThrownBy(() -> postService.createPost(new CreatePostRequest("   ", "sinh viên", null)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_CONTENT_OR_IMAGE_REQUIRED);

        verify(postHashtagRepository, never()).saveAndFlush(any(PostHashtag.class));
        verify(transactionTemplate, never()).execute(any());
    }

    @Test
    void createPostRejectsUserWithoutCompletedProfile() {
        UserProfile profile = completedProfile(10L);
        profile.setProfileCompletedAt(null);
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> postService.createPost(new CreatePostRequest("Noi dung", null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROFILE_NOT_COMPLETED);

        verify(cloudinaryStorageService, never()).uploadPostImage(any());
    }

    @Test
    void createPostCleansPreviousImagesWhenSecondUploadFails() {
        MockMultipartFile first = png("first.png");
        MockMultipartFile second = png("second.png");
        when(cloudinaryStorageService.uploadPostImage(first))
                .thenReturn(upload("https://cdn.example/first.png", "post/first", 100, 100));
        when(cloudinaryStorageService.uploadPostImage(second))
                .thenThrow(new BusinessException(ErrorCode.POST_IMAGE_UPLOAD_FAILED));

        assertThatThrownBy(() -> postService.createPost(new CreatePostRequest(null, null, List.of(first, second))))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_IMAGE_UPLOAD_FAILED);

        verify(cloudinaryStorageService).deletePostMedia("post/first", PostMediaType.IMAGE);
        verify(transactionTemplate, never()).execute(any());
    }

    @Test
    void createPostCleansAllUploadedImagesWhenDatabaseTransactionFails() {
        MockMultipartFile first = png("first.png");
        MockMultipartFile second = png("second.png");
        when(cloudinaryStorageService.uploadPostImage(first))
                .thenReturn(upload("https://cdn.example/first.png", "post/first", 100, 100));
        when(cloudinaryStorageService.uploadPostImage(second))
                .thenReturn(upload("https://cdn.example/second.png", "post/second", 100, 100));
        when(postRepository.saveAndFlush(any(Post.class))).thenThrow(new IllegalStateException("database failed"));

        assertThatThrownBy(() -> postService.createPost(new CreatePostRequest(null, null, List.of(first, second))))
                .isInstanceOf(IllegalStateException.class);

        verify(cloudinaryStorageService).deletePostMedia("post/first", PostMediaType.IMAGE);
        verify(cloudinaryStorageService).deletePostMedia("post/second", PostMediaType.IMAGE);
    }

    @Test
    void createPostCreatesExactlyOnePostHashtagAfterNormalization() {
        postService.createPost(new CreatePostRequest("Noi dung", "#Sinh #Vien", null));

        ArgumentCaptor<PostHashtag> captor = ArgumentCaptor.forClass(PostHashtag.class);
        verify(postHashtagRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getHashtag().getNormalizedName()).isEqualTo("sinh vien");
    }

    @Test
    void createPostCanReuseHashtagWhenTwoRequestsCreateSameNormalizedName() {
        CreatePostRequest firstRequest = new CreatePostRequest("Bai 1", "#DoAn", null);
        CreatePostRequest secondRequest = new CreatePostRequest("Bai 2", "doan", null);

        PostResponse firstResponse = postService.createPost(firstRequest);
        PostResponse secondResponse = postService.createPost(secondRequest);

        assertThat(firstResponse.hashtag()).isEqualTo("doan");
        assertThat(secondResponse.hashtag()).isEqualTo("doan");
        verify(hashtagRepository, times(2)).insertIfAbsent("doan", "doan");
    }

    @Test
    void getPostDetailReturnsPublishedPostWithViewerOwnerAndOrderedMetadata() {
        User author = user(10L);
        UserProfile authorProfile = completedProfile(10L);
        Post post = existingPost(1L, author, authorProfile);
        PostMedia firstMedia = postMedia(post, 301L, 0, "https://cdn.example/first.png");
        PostMedia secondMedia = postMedia(post, 302L, 1, "https://cdn.example/second.png");
        Hashtag hashtag = hashtag("sinhvien", 401L);
        when(postRepository.findDetailHeaderByIdAndStatus(1L, PostStatus.PUBLISHED)).thenReturn(Optional.of(post));
        when(postMediaRepository.findByPost_IdOrderByDisplayOrderAsc(1L)).thenReturn(List.of(firstMedia, secondMedia));
        when(postHashtagRepository.findWithHashtagByPostId(1L)).thenReturn(List.of(new PostHashtag(post, hashtag)));
        when(postLikeRepository.existsByIdUserIdAndIdPostId(10L, 1L)).thenReturn(true);

        var response = postService.getPostDetail(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.author().id()).isEqualTo(10L);
        assertThat(response.viewer().owner()).isTrue();
        assertThat(response.viewer().likedByCurrentUser()).isTrue();
        assertThat(response.media()).extracting("displayOrder").containsExactly(0, 1);
        assertThat(response.hashtag()).isEqualTo("sinhvien");
    }

    @Test
    void getOwnedPostDetailReturnsDeletedPostOnlyForItsAuthor() {
        User author = user(10L);
        Post post = existingPost(1L, author, completedProfile(10L));
        LocalDateTime deletedAt = LocalDateTime.of(2026, 7, 3, 1, 8);
        post.setStatus(PostStatus.DELETED);
        post.setDeletedAt(deletedAt);
        when(postRepository.findOwnedDetailHeaderById(1L, 10L)).thenReturn(Optional.of(post));
        when(postMediaRepository.findByPost_IdOrderByDisplayOrderAsc(1L)).thenReturn(List.of());
        when(postHashtagRepository.findWithHashtagByPostId(1L)).thenReturn(List.of());

        var response = postService.getOwnedPostDetailAs(10L, 1L);

        assertThat(response.status()).isEqualTo(PostStatus.DELETED);
        assertThat(response.deletedAt()).isEqualTo(deletedAt);
        assertThat(response.viewer().owner()).isTrue();
        verify(postRepository).findOwnedDetailHeaderById(1L, 10L);
        verify(postRepository, never()).findDetailHeaderByIdAndStatus(1L, PostStatus.PUBLISHED);
    }

    @Test
    void getPostDetailRejectsViewerWithoutCompletedProfile() {
        UserProfile profile = completedProfile(10L);
        profile.setProfileCompletedAt(null);
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> postService.getPostDetail(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROFILE_NOT_COMPLETED);

        verify(postRepository, never()).findDetailHeaderByIdAndStatus(any(), any());
    }

    @Test
    void getPostDetailReturnsPostNotFoundWhenPostIsHiddenDeletedOrMissing() {
        when(postRepository.findDetailHeaderByIdAndStatus(1L, PostStatus.PUBLISHED)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostDetail(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    @Test
    void getPostDetailReturnsPostNotFoundWhenAuthorIsBlocked() {
        User blockedAuthor = user(20L);
        blockedAuthor.setStatus(UserStatus.BLOCKED);
        Post post = existingPost(1L, blockedAuthor, completedProfile(20L));
        when(postRepository.findDetailHeaderByIdAndStatus(1L, PostStatus.PUBLISHED)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.getPostDetail(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_NOT_FOUND);

        verify(postMediaRepository, never()).findByPost_IdOrderByDisplayOrderAsc(any());
    }

    @Test
    void updatePostUpdatesContentHashtagsAndImagesWithinFifteenMinutes() {
        User author = user(10L);
        UserProfile authorProfile = completedProfile(10L);
        Post post = existingPost(1L, author, authorProfile);
        PostMedia kept = postMedia(post, 301L, 0, "https://cdn.example/old-1.png");
        PostMedia removed = postMedia(post, 302L, 1, "https://cdn.example/old-2.png");
        PostMedia newMedia = postMedia(post, 303L, 1, "https://cdn.example/new.png");
        MockMultipartFile newImage = png("new.png");
        when(postRepository.findDetailHeaderByIdAndStatus(1L, PostStatus.PUBLISHED)).thenReturn(Optional.of(post));
        when(postMediaRepository.findByPost_IdOrderByDisplayOrderAsc(1L))
                .thenReturn(List.of(kept, removed))
                .thenReturn(List.of(kept, newMedia));
        when(postMediaRepository.findByPost_IdAndIdIn(any(), any())).thenReturn(List.of(kept));
        when(postHashtagRepository.findWithHashtagByPostId(1L))
                .thenReturn(List.of(new PostHashtag(post, hashtag("cu", 400L))));
        when(cloudinaryStorageService.uploadPostImage(newImage))
                .thenReturn(upload("https://cdn.example/new.png", "post/new", 100, 100));

        var response = postService.updatePost(
                1L,
                new UpdatePostRequest("  Noi dung moi  ", "#DoAn", List.of(301L), List.of(newImage))
        );

        assertThat(post.getContent()).isEqualTo("Noi dung moi");
        assertThat(post.isEdited()).isTrue();
        assertThat(response.media()).extracting("id").containsExactly(301L, 303L);
        assertThat(response.hashtag()).isEqualTo("doan");
        verify(postMediaRepository).deleteAll(List.of(removed));
        verify(postHashtagRepository).deleteByPostId(1L);
        verify(postRepository).markEdited(1L);
    }

    @Test
    void updatePostAcceptsKeptVideoWithThreeNewImages() {
        Post post = existingPost(1L, user(10L), completedProfile(10L));
        PostMedia keptVideo = postMedia(post, 301L, 0, "https://cdn.example/video.mp4");
        keptVideo.setMediaType(PostMediaType.VIDEO);
        keptVideo.setDurationSeconds(60);
        MockMultipartFile firstImage = png("first.png");
        MockMultipartFile secondImage = jpeg("second.jpg");
        MockMultipartFile thirdImage = webp("third.webp");
        when(postRepository.findDetailHeaderByIdAndStatus(1L, PostStatus.PUBLISHED)).thenReturn(Optional.of(post));
        when(postMediaRepository.findByPost_IdOrderByDisplayOrderAsc(1L))
                .thenReturn(List.of(keptVideo));
        when(postMediaRepository.findByPost_IdAndIdIn(eq(1L), any())).thenReturn(List.of(keptVideo));
        when(postHashtagRepository.findWithHashtagByPostId(1L)).thenReturn(List.of());
        when(cloudinaryStorageService.uploadPostImage(firstImage))
                .thenReturn(upload("https://cdn.example/first.png", "post/first", 640, 480));
        when(cloudinaryStorageService.uploadPostImage(secondImage))
                .thenReturn(upload("https://cdn.example/second.jpg", "post/second", 640, 480));
        when(cloudinaryStorageService.uploadPostImage(thirdImage))
                .thenReturn(upload("https://cdn.example/third.webp", "post/third", 640, 480));

        postService.updatePost(1L, new UpdatePostRequest(
                "Noi dung moi", null, List.of(301L), List.of(firstImage, secondImage, thirdImage)
        ));

        verify(cloudinaryStorageService, times(3)).uploadPostImage(any());
        verify(cloudinaryStorageService, never()).uploadPostVideo(any());
    }

    @Test
    void updatePostKeepsHashtagWhenFieldIsAbsent() {
        Post post = existingPost(1L, user(10L), completedProfile(10L));
        PostHashtag current = new PostHashtag(post, hashtag("do an", 401L));
        prepareSimpleUpdate(post, List.of(current));

        var response = postService.updatePost(1L, new UpdatePostRequest("Noi dung moi", null, null, null));

        assertThat(response.hashtag()).isEqualTo("do an");
        verify(postHashtagRepository, never()).deleteByPostId(any());
        verify(postHashtagRepository, never()).saveAndFlush(any(PostHashtag.class));
    }

    @Test
    void updatePostDeletesHashtagWhenFieldContainsOnlyWhitespace() {
        Post post = existingPost(1L, user(10L), completedProfile(10L));
        PostHashtag current = new PostHashtag(post, hashtag("do an", 401L));
        prepareSimpleUpdate(post, List.of(current));

        var response = postService.updatePost(1L, new UpdatePostRequest("Noi dung moi", " \u00A0 ", null, null));

        assertThat(response.hashtag()).isNull();
        verify(postHashtagRepository).deleteByPostId(1L);
        verify(postHashtagRepository).flush();
        verify(postHashtagRepository, never()).saveAndFlush(any(PostHashtag.class));
    }

    @Test
    void updatePostDoesNotRewriteSameNormalizedHashtag() {
        Post post = existingPost(1L, user(10L), completedProfile(10L));
        PostHashtag current = new PostHashtag(post, hashtag("đồ án", 401L));
        prepareSimpleUpdate(post, List.of(current));

        var response = postService.updatePost(1L, new UpdatePostRequest("Noi dung moi", " #Đồ   Án ", null, null));

        assertThat(response.hashtag()).isEqualTo("đồ án");
        verify(postHashtagRepository, never()).deleteByPostId(any());
        verify(postHashtagRepository, never()).saveAndFlush(any(PostHashtag.class));
    }

    @Test
    void updatePostDefaultsLocationActionToKeep() {
        Post post = existingPost(1L, user(10L), completedProfile(10L));
        Location current = location("ChIJ-old", "Địa điểm cũ", 501L);
        post.setLocation(current);
        prepareSimpleUpdate(post, List.of());

        postService.updatePost(1L, new UpdatePostRequest("Nội dung mới", null, null, null));

        assertThat(post.getLocation()).isSameAs(current);
        verify(locationResolver, never()).resolve(any());
    }

    @Test
    void updatePostReplacesAndRemovesLocationWithoutDeletingSharedRow() {
        Post post = existingPost(1L, user(10L), completedProfile(10L));
        post.setLocation(location("ChIJ-old", "Địa điểm cũ", 501L));
        Location replacement = location("ChIJ-new", "Địa điểm mới", 502L);
        when(locationResolver.resolve(any(PostLocationRequest.class))).thenReturn(replacement);
        prepareSimpleUpdate(post, List.of());

        postService.updatePost(1L, new UpdatePostRequest("Nội dung mới", null, null, null,
                LocationAction.REPLACE, locationRequest("ChIJ-new")));
        assertThat(post.getLocation()).isSameAs(replacement);

        postService.updatePost(1L, new UpdatePostRequest("Nội dung mới lần hai", null, null, null,
                LocationAction.REMOVE, null));
        assertThat(post.getLocation()).isNull();
    }

    @Test
    void getPostDetailFailsWhenLegacyDataHasMultipleHashtags() {
        User author = user(10L);
        Post post = existingPost(1L, author, completedProfile(10L));
        when(postRepository.findDetailHeaderByIdAndStatus(1L, PostStatus.PUBLISHED)).thenReturn(Optional.of(post));
        when(postMediaRepository.findByPost_IdOrderByDisplayOrderAsc(1L)).thenReturn(List.of());
        when(postHashtagRepository.findWithHashtagByPostId(1L)).thenReturn(List.of(
                new PostHashtag(post, hashtag("mot", 401L)),
                new PostHashtag(post, hashtag("hai", 402L))));

        assertThatThrownBy(() -> postService.getPostDetail(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INTERNAL_ERROR);
    }

    @Test
    void updatePostRejectsNonAuthor() {
        Post post = existingPost(1L, user(20L), completedProfile(20L));
        when(postRepository.findDetailHeaderByIdAndStatus(1L, PostStatus.PUBLISHED)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.updatePost(1L, new UpdatePostRequest("Moi", null, null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_FORBIDDEN);
    }

    @Test
    void updatePostRejectsExpiredEditWindow() {
        Post post = existingPost(1L, user(10L), completedProfile(10L));
        ReflectionTestUtils.setField(post, "publishedAt", LocalDateTime.of(2026, 7, 3, 0, 50));
        when(postRepository.findDetailHeaderByIdAndStatus(1L, PostStatus.PUBLISHED)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.updatePost(1L, new UpdatePostRequest("Moi", null, null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_EDIT_TIME_EXPIRED);
    }

    @Test
    void updatePostAllowsEditThreeMinutesAfterPublished() {
        Post post = existingPost(1L, user(10L), completedProfile(10L));
        ReflectionTestUtils.setField(post, "publishedAt", LocalDateTime.of(2026, 7, 3, 1, 7));
        prepareSimpleUpdate(post, List.of());

        var response = postService.updatePost(1L, new UpdatePostRequest("Nội dung sau ba phút", null, null, null));

        assertThat(response.content()).isEqualTo("Nội dung sau ba phút");
    }

    @Test
    void updatePostSkipsModerationWhenNormalizedTextIsUnchanged() {
        Post post = existingPost(1L, user(10L), completedProfile(10L));
        prepareSimpleUpdate(post, List.of());

        postService.updatePost(1L, new UpdatePostRequest("  Noi dung  ", null, null, null));

        verify(contentModerationService, never()).requireAllowed(any());
    }

    @Test
    void updatePostDoesNotMutateWhenChangedTextIsRejected() {
        Post post = existingPost(1L, user(10L), completedProfile(10L));
        prepareSimpleUpdate(post, List.of());
        doThrow(new BusinessException(ErrorCode.CONTENT_MODERATION_WARNING))
                .when(contentModerationService).requireAllowed("Nội dung mới");

        assertThatThrownBy(() -> postService.updatePost(
                1L, new UpdatePostRequest("Nội dung mới", null, null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CONTENT_MODERATION_WARNING);

        assertThat(post.getContent()).isEqualTo("Noi dung");
        verify(transactionTemplate, never()).execute(any());
        verify(postRepository, never()).markEdited(any());
    }

    @Test
    void updatePostRejectsAtExactFifteenMinuteDeadline() {
        Post post = existingPost(1L, user(10L), completedProfile(10L));
        ReflectionTestUtils.setField(post, "publishedAt", LocalDateTime.of(2026, 7, 3, 0, 55));
        when(postRepository.findDetailHeaderByIdAndStatus(1L, PostStatus.PUBLISHED)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.updatePost(1L, new UpdatePostRequest("Mới", null, null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_EDIT_TIME_EXPIRED);
    }

    @Test
    void updatePostRejectsEmptyContentAndNoRemainingImages() {
        Post post = existingPost(1L, user(10L), completedProfile(10L));
        when(postRepository.findDetailHeaderByIdAndStatus(1L, PostStatus.PUBLISHED)).thenReturn(Optional.of(post));
        when(postMediaRepository.findByPost_IdOrderByDisplayOrderAsc(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> postService.updatePost(1L, new UpdatePostRequest("   ", null, List.of(), null)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_CONTENT_REQUIRED);
    }

    @Test
    void deletePostSoftDeletesPublishedPostForAuthor() {
        Post post = existingPost(1L, user(10L), completedProfile(10L));
        when(postRepository.findDetailHeaderByIdAndStatus(1L, PostStatus.PUBLISHED)).thenReturn(Optional.of(post));
        when(postRepository.softDeletePublishedPost(eq(1L), any(LocalDateTime.class))).thenReturn(1);

        DeletePostResponse response = postService.deletePost(1L);

        ArgumentCaptor<LocalDateTime> deletedAtCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        assertThat(response.postId()).isEqualTo(1L);
        assertThat(response.deleted()).isTrue();
        verify(postRepository).softDeletePublishedPost(eq(1L), deletedAtCaptor.capture());
        assertThat(deletedAtCaptor.getValue()).isEqualTo(LocalDateTime.of(2026, 7, 3, 1, 10));
        verify(cloudinaryStorageService, never()).deletePostMedia(any(), any());
        verify(postMediaRepository, never()).deleteAll(any());
    }

    @Test
    void deletePostRejectsNonAuthor() {
        Post post = existingPost(1L, user(20L), completedProfile(20L));
        when(postRepository.findDetailHeaderByIdAndStatus(1L, PostStatus.PUBLISHED)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.deletePost(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_FORBIDDEN);

        verify(postRepository, never()).softDeletePublishedPost(any(), any());
    }

    @Test
    void deletePostReturnsNotFoundWhenPostIsHiddenDeletedOrMissing() {
        when(postRepository.findDetailHeaderByIdAndStatus(1L, PostStatus.PUBLISHED)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.deletePost(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_NOT_FOUND);

        verify(postRepository, never()).softDeletePublishedPost(any(), any());
    }

    @Test
    void deletePostRejectsViewerWithoutCompletedProfile() {
        UserProfile profile = completedProfile(10L);
        profile.setProfileCompletedAt(null);
        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> postService.deletePost(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROFILE_NOT_COMPLETED);

        verify(postRepository, never()).findDetailHeaderByIdAndStatus(any(), any());
        verify(postRepository, never()).softDeletePublishedPost(any(), any());
    }

    @Test
    void deletePostReturnsNotFoundWhenConcurrentUpdateAlreadyChangedStatus() {
        Post post = existingPost(1L, user(10L), completedProfile(10L));
        when(postRepository.findDetailHeaderByIdAndStatus(1L, PostStatus.PUBLISHED)).thenReturn(Optional.of(post));
        when(postRepository.softDeletePublishedPost(eq(1L), any(LocalDateTime.class))).thenReturn(0);

        assertThatThrownBy(() -> postService.deletePost(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_NOT_FOUND);
    }

    @Test
    void createPostDoesNotHideOriginalExceptionWhenCleanupFails() {
        MockMultipartFile first = png("first.png");
        MockMultipartFile second = png("second.png");
        when(cloudinaryStorageService.uploadPostImage(first))
                .thenReturn(upload("https://cdn.example/first.png", "post/first", 100, 100));
        when(cloudinaryStorageService.uploadPostImage(second))
                .thenThrow(new BusinessException(ErrorCode.POST_IMAGE_UPLOAD_FAILED));
        doThrow(new BusinessException(ErrorCode.AVATAR_DELETE_FAILED))
                .when(cloudinaryStorageService).deletePostMedia("post/first", PostMediaType.IMAGE);

        assertThatThrownBy(() -> postService.createPost(new CreatePostRequest(null, null, List.of(first, second))))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_IMAGE_UPLOAD_FAILED);
    }

    private Post savedPost(Post post) {
        ReflectionTestUtils.setField(post, "id", postIds.incrementAndGet());
        ReflectionTestUtils.setField(post, "publishedAt", LocalDateTime.of(2026, 7, 3, 1, 0));
        ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.of(2026, 7, 3, 1, 0));
        ReflectionTestUtils.setField(post, "updatedAt", LocalDateTime.of(2026, 7, 3, 1, 0));
        return post;
    }

    private PostLocationRequest locationRequest(String placeId) {
        return new PostLocationRequest(placeId, "Đại học STU", null,
                BigDecimal.valueOf(10.7382456), BigDecimal.valueOf(106.6778123));
    }

    private Location location(String placeId, String displayName, Long id) {
        Location location = new Location(placeId, displayName, null,
                BigDecimal.valueOf(10.7382456), BigDecimal.valueOf(106.6778123));
        ReflectionTestUtils.setField(location, "id", id);
        return location;
    }

    private void prepareSimpleUpdate(Post post, List<PostHashtag> currentHashtags) {
        when(postRepository.findDetailHeaderByIdAndStatus(1L, PostStatus.PUBLISHED)).thenReturn(Optional.of(post));
        when(postMediaRepository.findByPost_IdOrderByDisplayOrderAsc(1L)).thenReturn(List.of());
        when(postHashtagRepository.findWithHashtagByPostId(1L)).thenReturn(currentHashtags);
    }

    private List<PostMedia> saveMedia(Iterable<PostMedia> mediaItems) {
        List<PostMedia> media = toList(mediaItems);
        for (PostMedia item : media) {
            if (item.getId() == null) {
                ReflectionTestUtils.setField(item, "id", mediaIds.incrementAndGet());
            }
        }
        return media;
    }

    private Post existingPost(Long postId, User author, UserProfile authorProfile) {
        Post post = new Post(author, "Noi dung");
        ReflectionTestUtils.setField(post, "id", postId);
        ReflectionTestUtils.setField(post, "publishedAt", LocalDateTime.of(2026, 7, 3, 1, 0));
        ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.of(2026, 7, 3, 1, 0));
        ReflectionTestUtils.setField(post, "updatedAt", LocalDateTime.of(2026, 7, 3, 1, 0));
        ReflectionTestUtils.setField(post, "authorProfile", authorProfile);
        post.setLikeCount(3);
        post.setCommentCount(2);
        return post;
    }

    private PostMedia postMedia(Post post, Long mediaId, Integer displayOrder, String url) {
        PostMedia media = new PostMedia(post, url, "post/" + mediaId, "image/png", 1234L, displayOrder);
        ReflectionTestUtils.setField(media, "id", mediaId);
        return media;
    }

    private Hashtag hashtag(String normalizedName, Long hashtagId) {
        Hashtag hashtag = new Hashtag(normalizedName, normalizedName);
        ReflectionTestUtils.setField(hashtag, "id", hashtagId);
        return hashtag;
    }

    private User user(Long userId) {
        User user = new User("student@example.com", "hash");
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }

    private UserProfile completedProfile(Long userId) {
        User user = user(userId);
        UserProfile profile = new UserProfile(user);
        ReflectionTestUtils.setField(profile, "userId", userId);
        profile.setDisplayName("Nguyen Van A");
        profile.setAvatarUrl("https://cdn.example/avatar.png");
        profile.setProfileCompletedAt(LocalDateTime.of(2026, 7, 3, 1, 0));
        return profile;
    }

    private CloudinaryUploadResult upload(String url, String publicId, Integer width, Integer height) {
        return new CloudinaryUploadResult(url, publicId, "image/png", 1234L, width, height);
    }

    private MockMultipartFile png(String filename) {
        return new MockMultipartFile("images", filename, "image/png", pngBytes());
    }

    private MockMultipartFile jpeg(String filename) {
        return new MockMultipartFile("images", filename, "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
    }

    private MockMultipartFile webp(String filename) {
        return new MockMultipartFile(
                "images",
                filename,
                "image/webp",
                new byte[]{0x52, 0x49, 0x46, 0x46, 0, 0, 0, 0, 0x57, 0x45, 0x42, 0x50}
        );
    }

    private MockMultipartFile mp4(String filename) {
        return new MockMultipartFile(
                "mediaFiles",
                filename,
                "video/mp4",
                new byte[]{0, 0, 0, 24, 0x66, 0x74, 0x79, 0x70, 0x69, 0x73, 0x6F, 0x6D}
        );
    }

    private byte[] pngBytes() {
        return new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    }

    private <T> List<T> toList(Iterable<T> values) {
        List<T> list = new ArrayList<>();
        values.forEach(list::add);
        return list;
    }
}
