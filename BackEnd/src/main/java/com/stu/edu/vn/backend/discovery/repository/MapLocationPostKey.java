package com.stu.edu.vn.backend.discovery.repository;

import java.time.LocalDateTime;

/** Projection khóa sắp xếp tối thiểu trước khi batch-load PostCard. */
public record MapLocationPostKey(Long postId, LocalDateTime publishedAt) {
}
