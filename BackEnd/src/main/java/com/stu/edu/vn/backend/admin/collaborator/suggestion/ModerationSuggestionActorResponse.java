package com.stu.edu.vn.backend.admin.collaborator.suggestion;

import java.util.Set;

/** Danh tính hiển thị cho Moderator, không bao gồm email hoặc dữ liệu xác thực. */
public record ModerationSuggestionActorResponse(
        Long adminId,
        String username,
        String displayName,
        String avatarUrl,
        Set<String> roles
) { }
