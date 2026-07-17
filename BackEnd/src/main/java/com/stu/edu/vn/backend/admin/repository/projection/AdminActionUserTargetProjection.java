package com.stu.edu.vn.backend.admin.repository.projection;

/** Dữ liệu tối thiểu để resolve hàng loạt target USER. */
public interface AdminActionUserTargetProjection {
    Long getTargetId();

    String getDisplayName();
}
