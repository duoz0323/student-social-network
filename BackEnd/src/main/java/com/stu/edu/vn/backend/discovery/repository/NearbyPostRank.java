package com.stu.edu.vn.backend.discovery.repository;

import java.time.LocalDateTime;

/** Projection tối thiểu của query Nearby trước khi batch-load PostCard. */
public record NearbyPostRank(Long postId, long distanceMeters, LocalDateTime publishedAt) {
}
