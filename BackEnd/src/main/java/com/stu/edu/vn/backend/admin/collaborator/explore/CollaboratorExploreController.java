package com.stu.edu.vn.backend.admin.collaborator.explore;

import com.stu.edu.vn.backend.admin.collaborator.identity.CollaboratorSocialIdentityResolver;
import com.stu.edu.vn.backend.admin.dto.response.AdminHashtagListItemResponse;
import com.stu.edu.vn.backend.admin.service.AdminHashtagService;
import com.stu.edu.vn.backend.common.api.*;
import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;
import com.stu.edu.vn.backend.feed.service.FeedService;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.search.dto.response.SearchPostResponse;
import com.stu.edu.vn.backend.search.enums.SearchPostType;
import com.stu.edu.vn.backend.search.service.SearchService;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** API đọc dùng query/service hiện hữu; Managed Social Identity là viewer của Feed. */
@Validated
@RestController
@RequestMapping("/api/v1/admin/collaborator")
@RequiredArgsConstructor
public class CollaboratorExploreController {
    private final FeedService feedService;
    private final SearchService searchService;
    private final AdminHashtagService hashtagService;
    private final CollaboratorSocialIdentityResolver identityResolver;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/explore")
    @PreAuthorize("hasAuthority('COLLABORATOR_EXPLORE_VIEW')")
    public ResponseEntity<ApiResponse<CursorPageResponse<FeedPostResponse>>> explore(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(20) int limit) {
        Long socialUserId = identityResolver.resolveActive(currentUserProvider.getCurrentUserId()).getId();
        return ResponseEntity.ok(ApiResponse.success("Khám phá nội dung thành công",
                feedService.getForYouAs(socialUserId, cursor, limit)));
    }

    @GetMapping("/explore/search")
    @PreAuthorize("hasAuthority('COLLABORATOR_EXPLORE_VIEW')")
    public ResponseEntity<ApiResponse<CursorPageResponse<SearchPostResponse>>> searchContent(
            @RequestParam("q") @NotBlank @Size(max = 100) String keyword,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(20) int limit) {
        Long socialUserId = identityResolver.resolveActive(currentUserProvider.getCurrentUserId()).getId();
        return ResponseEntity.ok(ApiResponse.success("Tìm nội dung khám phá thành công",
                searchService.searchPostsAs(socialUserId, keyword, SearchPostType.CONTENT, cursor, limit)));
    }

    @GetMapping("/hashtags")
    @PreAuthorize("hasAuthority('COLLABORATOR_HASHTAG_VIEW') and (#keyword == null or hasAuthority('COLLABORATOR_HASHTAG_SEARCH'))")
    public ResponseEntity<ApiResponse<PageResponse<AdminHashtagListItemResponse>>> hashtags(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ApiResponse.success("Lấy hashtag thành công",
                hashtagService.getHashtags(keyword, page, size)));
    }
}
