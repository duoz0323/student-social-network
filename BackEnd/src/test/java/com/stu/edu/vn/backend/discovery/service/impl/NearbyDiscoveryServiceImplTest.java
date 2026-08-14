package com.stu.edu.vn.backend.discovery.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.cursor.CursorCodec;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.discovery.cursor.NearbyCursor;
import com.stu.edu.vn.backend.discovery.model.NearbyBoundingBox;
import com.stu.edu.vn.backend.discovery.repository.NearbyDiscoveryRepository;
import com.stu.edu.vn.backend.discovery.repository.NearbyPostRank;
import com.stu.edu.vn.backend.discovery.security.DiscoveryViewerGuard;
import com.stu.edu.vn.backend.discovery.service.NearbyQuerySupport;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;
import com.stu.edu.vn.backend.feed.service.FeedPostBatchLoader;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NearbyDiscoveryServiceImplTest {
    private static final double LATITUDE = 10.8231d;
    private static final double LONGITUDE = 106.6297d;

    @Mock private DiscoveryViewerGuard viewerGuard;
    @Mock private CursorCodec cursorCodec;
    @Mock private NearbyDiscoveryRepository nearbyDiscoveryRepository;
    @Mock private PostRepository postRepository;
    @Mock private FeedPostBatchLoader feedPostBatchLoader;

    private NearbyQuerySupport querySupport;
    private NearbyDiscoveryServiceImpl service;

    @BeforeEach
    void setUp() {
        querySupport = new NearbyQuerySupport();
        service = new NearbyDiscoveryServiceImpl(
                viewerGuard,
                querySupport,
                cursorCodec,
                nearbyDiscoveryRepository,
                postRepository,
                feedPostBatchLoader
        );
        when(viewerGuard.requireEligibleViewer()).thenReturn(
                new CustomUserPrincipal(7L, UserRole.USER, UserStatus.ACTIVE));
    }

    @Test
    void fetchesLimitPlusOnePreservesRankOrderAndUsesLastReturnedItemForCursor() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 13, 10, 0);
        List<NearbyPostRank> ranks = List.of(
                new NearbyPostRank(11L, 100L, now),
                new NearbyPostRank(10L, 100L, now.minusMinutes(1)),
                new NearbyPostRank(9L, 200L, now)
        );
        when(nearbyDiscoveryRepository.findNearby(
                eq(7L), eq(LATITUDE), eq(LONGITUDE), eq(5), any(NearbyBoundingBox.class), isNull(), eq(3)))
                .thenReturn(ranks);
        Post first = post(11L);
        Post second = post(10L);
        when(postRepository.findFeedHeadersByIds(List.of(11L, 10L))).thenReturn(List.of(second, first));
        FeedPostResponse firstResponse = org.mockito.Mockito.mock(FeedPostResponse.class);
        FeedPostResponse secondResponse = org.mockito.Mockito.mock(FeedPostResponse.class);
        when(feedPostBatchLoader.map(List.of(first, second), 7L))
                .thenReturn(List.of(firstResponse, secondResponse));
        when(cursorCodec.encode(any(NearbyCursor.class))).thenReturn("next");

        var result = service.getNearby(LATITUDE, LONGITUDE, 5, 2, null);

        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo("next");
        assertThat(result.content()).extracting(item -> item.post())
                .containsExactly(firstResponse, secondResponse);
        assertThat(result.content()).extracting(item -> item.distanceMeters())
                .containsExactly(100L, 100L);
        ArgumentCaptor<NearbyCursor> cursorCaptor = ArgumentCaptor.forClass(NearbyCursor.class);
        verify(cursorCodec).encode(cursorCaptor.capture());
        assertThat(cursorCaptor.getValue().distanceMeters()).isEqualTo(100L);
        assertThat(cursorCaptor.getValue().publishedAt()).isEqualTo(now.minusMinutes(1));
        assertThat(cursorCaptor.getValue().postId()).isEqualTo(10L);
        assertThat(cursorCaptor.getValue().version()).isEqualTo(NearbyCursor.CURRENT_VERSION);
    }

    @Test
    void validCursorIsPassedToRepositoryAndLastPageHasNoNextCursor() {
        String fingerprint = querySupport.fingerprint(LATITUDE, LONGITUDE, 5);
        NearbyCursor cursor = new NearbyCursor(
                NearbyCursor.CURRENT_VERSION, 850L,
                LocalDateTime.of(2026, 8, 13, 9, 0), 20L, fingerprint);
        when(cursorCodec.decode("valid", NearbyCursor.class)).thenReturn(cursor);
        when(nearbyDiscoveryRepository.findNearby(
                eq(7L), eq(LATITUDE), eq(LONGITUDE), eq(5), any(NearbyBoundingBox.class), eq(cursor), eq(11)))
                .thenReturn(List.of());

        var result = service.getNearby(LATITUDE, LONGITUDE, 5, 10, "valid");

        assertThat(result.content()).isEmpty();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
        verify(cursorCodec, never()).encode(any());
    }

    @Test
    void rejectsCursorWithWrongVersionCoordinateOrRadiusBeforeQuery() {
        String baseline = querySupport.fingerprint(LATITUDE, LONGITUDE, 5);
        LocalDateTime time = LocalDateTime.of(2026, 8, 13, 9, 0);
        NearbyCursor wrongVersion = new NearbyCursor(99, 1L, time, 1L, baseline);
        when(cursorCodec.decode("wrong-version", NearbyCursor.class)).thenReturn(wrongVersion);
        assertBusinessError(
                () -> service.getNearby(LATITUDE, LONGITUDE, 5, 10, "wrong-version"),
                ErrorCode.INVALID_CURSOR);

        NearbyCursor otherCoordinate = new NearbyCursor(
                NearbyCursor.CURRENT_VERSION, 1L, time, 1L,
                querySupport.fingerprint(LATITUDE + 0.001d, LONGITUDE, 5));
        when(cursorCodec.decode("other-coordinate", NearbyCursor.class)).thenReturn(otherCoordinate);
        assertBusinessError(
                () -> service.getNearby(LATITUDE, LONGITUDE, 5, 10, "other-coordinate"),
                ErrorCode.INVALID_CURSOR);

        NearbyCursor otherRadius = new NearbyCursor(
                NearbyCursor.CURRENT_VERSION, 1L, time, 1L,
                querySupport.fingerprint(LATITUDE, LONGITUDE, 10));
        when(cursorCodec.decode("other-radius", NearbyCursor.class)).thenReturn(otherRadius);
        assertBusinessError(
                () -> service.getNearby(LATITUDE, LONGITUDE, 5, 10, "other-radius"),
                ErrorCode.INVALID_CURSOR);

        verify(nearbyDiscoveryRepository, never()).findNearby(any(), any(Double.class), any(Double.class),
                any(Integer.class), any(), any(), any(Integer.class));
    }

    @Test
    void propagatesViewerGuardErrorBeforeQuery() {
        when(viewerGuard.requireEligibleViewer()).thenThrow(new BusinessException(ErrorCode.FORBIDDEN));

        assertBusinessError(() -> service.getNearby(LATITUDE, LONGITUDE, 5, 10, null), ErrorCode.FORBIDDEN);
        verify(nearbyDiscoveryRepository, never()).findNearby(any(), any(Double.class), any(Double.class),
                any(Integer.class), any(), any(), any(Integer.class));
    }

    private Post post(long id) {
        Post post = org.mockito.Mockito.mock(Post.class);
        when(post.getId()).thenReturn(id);
        return post;
    }

    private void assertBusinessError(Runnable action, ErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }
}
