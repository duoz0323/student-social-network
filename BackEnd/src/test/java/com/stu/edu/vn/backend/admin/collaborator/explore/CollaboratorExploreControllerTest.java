package com.stu.edu.vn.backend.admin.collaborator.explore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.admin.collaborator.identity.CollaboratorSocialIdentityResolver;
import com.stu.edu.vn.backend.admin.service.AdminHashtagService;
import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.feed.service.FeedService;
import com.stu.edu.vn.backend.search.enums.SearchPostType;
import com.stu.edu.vn.backend.search.dto.response.SearchPostResponse;
import com.stu.edu.vn.backend.search.service.SearchService;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import java.util.List;
import org.junit.jupiter.api.Test;

class CollaboratorExploreControllerTest {

    @Test
    void searchContentUsesManagedSocialIdentityAsViewer() {
        FeedService feedService = mock(FeedService.class);
        SearchService searchService = mock(SearchService.class);
        AdminHashtagService hashtagService = mock(AdminHashtagService.class);
        CollaboratorSocialIdentityResolver identityResolver = mock(CollaboratorSocialIdentityResolver.class);
        CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
        User socialUser = mock(User.class);
        CursorPageResponse<SearchPostResponse> emptyPage = new CursorPageResponse<>(List.of(), null, false);

        when(currentUserProvider.getCurrentUserId()).thenReturn(7L);
        when(identityResolver.resolveActive(7L)).thenReturn(socialUser);
        when(socialUser.getId()).thenReturn(42L);
        when(searchService.searchPostsAs(42L, "tình nguyện", SearchPostType.CONTENT, null, 20))
                .thenReturn(emptyPage);

        CollaboratorExploreController controller = new CollaboratorExploreController(
                feedService, searchService, hashtagService, identityResolver, currentUserProvider);

        var response = controller.searchContent("tình nguyện", null, 20);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data().content()).isEmpty();
        verify(searchService).searchPostsAs(42L, "tình nguyện", SearchPostType.CONTENT, null, 20);
    }
}
