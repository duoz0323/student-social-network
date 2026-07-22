package com.stu.edu.vn.backend.follow.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.follow.dto.response.FollowUserResponse;
import com.stu.edu.vn.backend.follow.repository.FollowUserProjection;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class FollowMapperTest {

    private final FollowMapper followMapper = new FollowMapper();

    @Test
    void mapperKeepsPublicFieldsAndTrueFollowState() {
        // Projection Boolean.TRUE phải được chuyển thành primitive true trong DTO công khai.
        FollowUserResponse response = followMapper.toResponse(projection(Boolean.TRUE));

        assertThat(response.userId()).isEqualTo(20L);
        assertThat(response.displayName()).isEqualTo("Nguyen Van B");
        assertThat(response.avatarUrl()).isEqualTo("https://cdn.example/avatar.png");
        assertThat(response.bio()).isEqualTo("Sinh vien CNTT");
        assertThat(response.followedAt()).isEqualTo(LocalDateTime.of(2026, 7, 12, 10, 0));
        assertThat(response.followedByCurrentUser()).isTrue();
    }

    @Test
    void mapperTreatsFalseAndNullAsNotFollowed() {
        // Boolean.TRUE.equals tránh NullPointerException nếu driver hoặc projection trả null ngoài dự kiến.
        assertThat(followMapper.toResponse(projection(Boolean.FALSE)).followedByCurrentUser()).isFalse();
        assertThat(followMapper.toResponse(projection(null)).followedByCurrentUser()).isFalse();
    }

    private FollowUserProjection projection(Boolean followedByCurrentUser) {
        return new FollowUserProjection() {
            public Long getUserId() { return 20L; }
            public String getDisplayName() { return "Nguyen Van B"; }
            public String getAvatarUrl() { return "https://cdn.example/avatar.png"; }
            public String getBio() { return "Sinh vien CNTT"; }
            public LocalDateTime getFollowedAt() { return LocalDateTime.of(2026, 7, 12, 10, 0); }
            public Boolean getFollowedByCurrentUser() { return followedByCurrentUser; }
        };
    }
}
