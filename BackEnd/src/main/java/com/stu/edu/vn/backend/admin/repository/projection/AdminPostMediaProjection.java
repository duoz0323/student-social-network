package com.stu.edu.vn.backend.admin.repository.projection;


/** Projection media chỉ chọn ba trường được phép trả ra API. */
public interface AdminPostMediaProjection {
    Long getMediaId();
    String getMediaUrl();
    String getMediaType();
    String getMimeType();
    Integer getDurationSeconds();
    String getThumbnailUrl();
    Integer getSortOrder();
}
