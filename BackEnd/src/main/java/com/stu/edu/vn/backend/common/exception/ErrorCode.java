package com.stu.edu.vn.backend.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Mã lỗi nghiệp vụ dùng chung để response lỗi nhất quán giữa các module.
 */
public enum ErrorCode {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ"),
    INVALID_IDENTIFIER(HttpStatus.BAD_REQUEST, "Email hoặc số điện thoại không hợp lệ"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Email, số điện thoại hoặc mật khẩu không đúng"),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Refresh Token không hợp lệ"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Refresh Token đã hết hạn"),
    REFRESH_TOKEN_REVOKED(HttpStatus.UNAUTHORIZED, "Refresh Token đã bị thu hồi"),
    PASSWORD_CONFIRMATION_NOT_MATCH(HttpStatus.BAD_REQUEST, "Xác nhận mật khẩu không khớp"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email đã được sử dụng"),
    PHONE_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "Số điện thoại đã được sử dụng"),
    REGISTER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể hoàn tất đăng ký, vui lòng thử lại sau"),
    PROFILE_NOT_COMPLETED(HttpStatus.FORBIDDEN, "Bạn cần hoàn tất hồ sơ trước khi sử dụng chức năng này"),
    PROFILE_ALREADY_COMPLETED(HttpStatus.CONFLICT, "Hồ sơ ban đầu đã được hoàn tất"),
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy hồ sơ người dùng"),
    INVALID_DISPLAY_NAME(HttpStatus.BAD_REQUEST, "Tên hiển thị phải từ 2 đến 100 ký tự"),
    INVALID_DATE_OF_BIRTH(HttpStatus.BAD_REQUEST, "Ngày sinh không hợp lệ"),
    USER_UNDER_MINIMUM_AGE(HttpStatus.BAD_REQUEST, "Người dùng phải đủ 18 tuổi"),
    BIO_TOO_LONG(HttpStatus.BAD_REQUEST, "Giới thiệu cá nhân không được vượt quá 500 ký tự"),
    AVATAR_FILE_REQUIRED(HttpStatus.BAD_REQUEST, "Vui lòng chọn ảnh đại diện"),
    AVATAR_FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "Ảnh đại diện không được vượt quá 10 MB"),
    AVATAR_FILE_TYPE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "Định dạng ảnh đại diện không được hỗ trợ"),
    AVATAR_MIME_TYPE_INVALID(HttpStatus.BAD_REQUEST, "Nội dung tệp ảnh đại diện không hợp lệ"),
    AVATAR_UPLOAD_FAILED(HttpStatus.BAD_GATEWAY, "Không thể tải ảnh đại diện lên hệ thống"),
    AVATAR_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể xóa ảnh đại diện"),
    CLOUDINARY_CONFIGURATION_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "Cấu hình lưu trữ ảnh chưa hợp lệ"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập để tiếp tục"),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "Access Token không hợp lệ"),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Access Token đã hết hạn"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"),
    USER_BLOCKED(HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy dữ liệu"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Hệ thống đang gặp lỗi, vui lòng thử lại sau");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
