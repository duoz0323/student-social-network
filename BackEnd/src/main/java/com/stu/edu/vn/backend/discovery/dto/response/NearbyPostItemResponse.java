package com.stu.edu.vn.backend.discovery.dto.response;

import com.stu.edu.vn.backend.feed.dto.FeedPostResponse;

/** Bọc PostCard chuẩn hiện hành để distance không làm thay đổi response dùng chung của các module khác. */
public record NearbyPostItemResponse(FeedPostResponse post, long distanceMeters) {
}
