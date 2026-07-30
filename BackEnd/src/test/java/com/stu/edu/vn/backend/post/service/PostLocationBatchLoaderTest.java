package com.stu.edu.vn.backend.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.location.entity.Location;
import com.stu.edu.vn.backend.location.repository.LocationRepository;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.user.entity.User;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PostLocationBatchLoaderTest {
    @Test
    void loadsDistinctLocationsOnceForWholePostPage() {
        Location location = new Location("ChIJ", "STU", null, BigDecimal.TEN, BigDecimal.valueOf(106));
        ReflectionTestUtils.setField(location, "id", 5L);
        Post first = post(1L, location);
        Post second = post(2L, location);
        LocationRepository repository = org.mockito.Mockito.mock(LocationRepository.class);
        when(repository.findAllById(List.of(5L))).thenReturn(List.of(location));

        var result = new PostLocationBatchLoader(repository).loadByPostId(List.of(first, second));

        assertThat(result).containsEntry(1L, location).containsEntry(2L, location);
        verify(repository, times(1)).findAllById(List.of(5L));
    }

    private Post post(Long id, Location location) {
        Post post = new Post(org.mockito.Mockito.mock(User.class), "Nội dung");
        ReflectionTestUtils.setField(post, "id", id);
        post.setLocation(location);
        return post;
    }
}
