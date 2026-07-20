package com.stu.edu.vn.backend.auth.service;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;

/** Conflict được trả sau khi challenge đã lưu thành công nên transaction không rollback riêng lỗi này. */
public class SocialConflictException extends BusinessException {
    public SocialConflictException(ErrorCode errorCode, Object details) {
        super(errorCode, details);
    }
}
