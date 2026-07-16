package com.stu.edu.vn.backend.admin.dto.request;

import com.stu.edu.vn.backend.admin.enums.AdminPostHideReason;
import com.fasterxml.jackson.annotation.JsonAnySetter;

/** Request ẩn bài chỉ nhận mã lý do cố định, không nhận ghi chú tự do hoặc adminId. */
public record AdminHidePostRequest(AdminPostHideReason reasonCode) {

    @JsonAnySetter
    public void rejectUnknownField(String fieldName, Object value) {
        // Từ chối note/reason hoặc mọi trường ngoài reasonCode để contract không nhận lý do tự do.
        throw new IllegalArgumentException("Trường request không được hỗ trợ: " + fieldName);
    }
}
