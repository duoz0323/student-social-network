package com.stu.edu.vn.backend.storage.cleanup;

/** Vòng đời retry xóa file đã trở thành rác. */
public enum MediaCleanupStatus {
    PENDING, PROCESSING, COMPLETED, FAILED
}
