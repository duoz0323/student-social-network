package com.stu.edu.vn.backend.messaging.dto.response;

/** Tác giả công khai trong preview chia sẻ; username chỉ mở rộng trong contract Messaging này. */
public record SharedPostAuthorResponse(Long userId, String username, String displayName, String avatarUrl) { }
