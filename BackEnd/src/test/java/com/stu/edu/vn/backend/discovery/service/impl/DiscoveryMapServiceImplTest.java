package com.stu.edu.vn.backend.discovery.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.common.cursor.CursorCodec;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.discovery.config.DiscoveryMapProperties;
import com.stu.edu.vn.backend.discovery.cursor.MapLocationPostsCursor;
import com.stu.edu.vn.backend.discovery.dto.response.MapLocationResponse;
import com.stu.edu.vn.backend.discovery.repository.DiscoveryMapRepository;
import com.stu.edu.vn.backend.discovery.repository.MapLocationPostKey;
import com.stu.edu.vn.backend.discovery.security.DiscoveryViewerGuard;
import com.stu.edu.vn.backend.discovery.service.DiscoveryMapQuerySupport;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;
import com.stu.edu.vn.backend.feed.service.FeedPostBatchLoader;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Kiểm tra orchestration max+1, cursor, thứ tự batch loader và số call O(1). */
@ExtendWith(MockitoExtension.class)
class DiscoveryMapServiceImplTest {
    @Mock private DiscoveryViewerGuard viewerGuard;
    @Mock private CursorCodec cursorCodec;
    @Mock private DiscoveryMapRepository repository;
    @Mock private PostRepository postRepository;
    @Mock private FeedPostBatchLoader feedPostBatchLoader;
    private DiscoveryMapProperties properties;
    private DiscoveryMapServiceImpl service;

    @BeforeEach
    void setUp() {
        properties = new DiscoveryMapProperties();
        service = new DiscoveryMapServiceImpl(
                viewerGuard,
                new DiscoveryMapQuerySupport(),
                properties,
                cursorCodec,
                repository,
                postRepository,
                feedPostBatchLoader);
        when(viewerGuard.requireEligibleViewer()).thenReturn(
                new CustomUserPrincipal(7L, UserRole.USER, UserStatus.ACTIVE));
    }

    @Test
    void markerFetchesMaxPlusOneDropsOverflowAndReportsTruncated() {
        properties.setMaxLocations(2);
        List<MapLocationResponse> markers = List.of(marker(3L), marker(2L), marker(1L));
        when(repository.findMapLocations(7L, 10.78d, 10.73d, 106.70d, 106.64d, 3))
                .thenReturn(markers);

        var result = service.getLocations(10.78d, 10.73d, 106.70d, 106.64d);

        assertThat(result.locations()).extracting(MapLocationResponse::locationId).containsExactly(3L, 2L);
        assertThat(result.truncated()).isTrue();
    }

    @Test
    void exactlyConfiguredMarkerLimitIsNotTruncated() {
        properties.setMaxLocations(2);
        when(repository.findMapLocations(7L, 1.0d, 0.0d, 1.0d, 0.0d, 3))
                .thenReturn(List.of(marker(2L), marker(1L)));

        assertThat(service.getLocations(1.0d, 0.0d, 1.0d, 0.0d).truncated()).isFalse();
    }

    @Test
    void defaultTwoHundredLimitUsesFetchTwoHundredAndOneSemantics() {
        List<MapLocationResponse> exactlyTwoHundred = java.util.stream.LongStream.rangeClosed(1, 200)
                .mapToObj(this::marker)
                .toList();
        when(repository.findMapLocations(7L, 1.0d, 0.0d, 1.0d, 0.0d, 201))
                .thenReturn(exactlyTwoHundred);
        assertThat(service.getLocations(1.0d, 0.0d, 1.0d, 0.0d))
                .satisfies(result -> {
                    assertThat(result.locations()).hasSize(200);
                    assertThat(result.truncated()).isFalse();
                });

        List<MapLocationResponse> twoHundredAndOne = java.util.stream.LongStream.rangeClosed(1, 201)
                .mapToObj(this::marker)
                .toList();
        when(repository.findMapLocations(7L, 2.0d, 0.0d, 2.0d, 0.0d, 201))
                .thenReturn(twoHundredAndOne);
        assertThat(service.getLocations(2.0d, 0.0d, 2.0d, 0.0d))
                .satisfies(result -> {
                    assertThat(result.locations()).hasSize(200);
                    assertThat(result.truncated()).isTrue();
                });
    }

    @Test
    void locationPostsFetchLimitPlusOnePreserveSqlOrderAndEncodeLastReturnedKey() {
        LocalDateTime time = LocalDateTime.of(2026, 8, 14, 1, 20);
        List<MapLocationPostKey> keys = List.of(
                new MapLocationPostKey(12L, time),
                new MapLocationPostKey(11L, time),
                new MapLocationPostKey(10L, time.minusMinutes(1)));
        when(repository.findLocationPostKeys(7L, 15L, null, 3)).thenReturn(keys);
        Post first = post(12L);
        Post second = post(11L);
        when(postRepository.findFeedHeadersByIds(List.of(12L, 11L))).thenReturn(List.of(second, first));
        FeedPostResponse firstResponse = org.mockito.Mockito.mock(FeedPostResponse.class);
        FeedPostResponse secondResponse = org.mockito.Mockito.mock(FeedPostResponse.class);
        when(feedPostBatchLoader.map(List.of(first, second), 7L))
                .thenReturn(List.of(firstResponse, secondResponse));
        when(cursorCodec.encode(any(MapLocationPostsCursor.class))).thenReturn("next");

        var result = service.getLocationPosts(15L, 2, null);

        assertThat(result.content()).containsExactly(firstResponse, secondResponse);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo("next");
        ArgumentCaptor<MapLocationPostsCursor> captor = ArgumentCaptor.forClass(MapLocationPostsCursor.class);
        verify(cursorCodec).encode(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new MapLocationPostsCursor(1, 15L, time, 11L));
    }

    @Test
    void rejectsCursorForAnotherLocationBeforeRepository() {
        MapLocationPostsCursor cursor = new MapLocationPostsCursor(
                1, 20L, LocalDateTime.of(2026, 8, 14, 1, 20), 100L);
        when(cursorCodec.decode("other", MapLocationPostsCursor.class)).thenReturn(cursor);

        assertThatThrownBy(() -> service.getLocationPosts(15L, 10, "other"))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CURSOR));
        verify(repository, never()).findLocationPostKeys(any(), any(), any(), any(Integer.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 20})
    void postLoadingQueryOrchestrationRemainsConstantForPageSizes(int pageSize) {
        List<MapLocationPostKey> keys = new ArrayList<>();
        List<Post> posts = new ArrayList<>();
        List<FeedPostResponse> responses = new ArrayList<>();
        LocalDateTime time = LocalDateTime.of(2026, 8, 14, 1, 20);
        for (int index = 0; index < pageSize; index++) {
            long id = pageSize - index;
            keys.add(new MapLocationPostKey(id, time.minusSeconds(index)));
            posts.add(post(id));
            responses.add(org.mockito.Mockito.mock(FeedPostResponse.class));
        }
        when(repository.findLocationPostKeys(7L, 15L, null, pageSize + 1)).thenReturn(keys);
        when(postRepository.findFeedHeadersByIds(keys.stream().map(MapLocationPostKey::postId).toList()))
                .thenReturn(posts);
        when(feedPostBatchLoader.map(posts, 7L)).thenReturn(responses);

        assertThat(service.getLocationPosts(15L, pageSize, null).content()).hasSize(pageSize);
        verify(repository).findLocationPostKeys(7L, 15L, null, pageSize + 1);
        verify(postRepository).findFeedHeadersByIds(keys.stream().map(MapLocationPostKey::postId).toList());
        verify(feedPostBatchLoader).map(posts, 7L);
        verify(cursorCodec, never()).encode(any());
    }

    @Test
    void emptyLocationReturnsEmptyPageWithoutPostLoader() {
        when(repository.findLocationPostKeys(7L, 999L, null, 11)).thenReturn(List.of());

        var result = service.getLocationPosts(999L, 10, null);

        assertThat(result.content()).isEmpty();
        assertThat(result.hasNext()).isFalse();
        verify(postRepository, never()).findFeedHeadersByIds(any());
        verify(feedPostBatchLoader, never()).map(any(), any());
    }

    private MapLocationResponse marker(long id) {
        return new MapLocationResponse(
                id, "Location " + id, null, BigDecimal.ONE, BigDecimal.ONE,
                1L, LocalDateTime.of(2026, 8, 14, 1, 20));
    }

    private Post post(long id) {
        Post post = org.mockito.Mockito.mock(Post.class);
        when(post.getId()).thenReturn(id);
        return post;
    }
}
