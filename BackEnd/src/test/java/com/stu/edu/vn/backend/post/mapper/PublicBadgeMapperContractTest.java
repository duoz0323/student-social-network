package com.stu.edu.vn.backend.post.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.feed.mapper.FeedPostMapper;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

class PublicBadgeMapperContractTest {

    private final PostMapper postMapper = Mappers.getMapper(PostMapper.class);
    private final FeedPostMapper feedPostMapper = Mappers.getMapper(FeedPostMapper.class);

    @Test
    void legacyAuthorHelpersReturnEmptyBadgeListInsteadOfNull() {
        UserProfile profile = Mockito.mock(UserProfile.class);
        when(profile.getUserId()).thenReturn(20L);
        when(profile.getDisplayName()).thenReturn("Nguyen Van B");
        when(profile.getAvatarUrl()).thenReturn("https://cdn.example/avatar.png");

        // Helper không nhận badge vẫn phải giữ contract collection rỗng, không trả null cho Client.
        assertThat(postMapper.toAuthorResponse(profile).badges()).isEmpty();
        assertThat(feedPostMapper.toAuthor(profile).badges()).isEmpty();
    }
}
