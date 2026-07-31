package com.stu.edu.vn.backend.admin.repository.projection;

/** Projection thống kê lý do theo case. */
public interface ModerationReasonCountProjection {
    Long getCaseId();
    String getReason();
    Long getReasonCount();
}
