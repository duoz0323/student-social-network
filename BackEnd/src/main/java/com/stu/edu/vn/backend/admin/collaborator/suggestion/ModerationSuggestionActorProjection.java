package com.stu.edu.vn.backend.admin.collaborator.suggestion;

/** Dữ liệu công khai tối thiểu của tài khoản Admin tham gia luồng đề xuất. */
public interface ModerationSuggestionActorProjection {
    Long getAdminId();
    String getUsername();
    String getDisplayName();
    String getAvatarUrl();
    String getRoleCodes();
}
