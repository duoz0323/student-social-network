package com.stu.edu.vn.backend.admin.dto.request;

import com.stu.edu.vn.backend.admin.enums.AdminBlockReason;
import com.fasterxml.jackson.annotation.JsonAnySetter;

/**
 * Request khóa tài khoản chỉ nhận mã lý do cố định, không nhận ghi chú hoặc chuỗi tự do.
 */
public record AdminBlockUserRequest(AdminBlockReason reasonCode) {

    @JsonAnySetter
    public void rejectUnknownField(String fieldName, Object value) {
        // Từ chối note/reason hoặc mọi trường tự do ngoài reasonCode để giữ contract khóa tài khoản cố định.
        throw new IllegalArgumentException("Trường request không được hỗ trợ: " + fieldName);
    }
}
