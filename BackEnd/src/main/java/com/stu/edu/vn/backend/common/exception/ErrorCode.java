package com.stu.edu.vn.backend.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Mã lỗi nghiệp vụ dùng chung để response lỗi nhất quán giữa các module.
 */
public enum ErrorCode {
    ADMIN_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản người dùng cần quản lý"),
    ADMIN_USER_MANAGEMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "Không được phép quản lý tài khoản ADMIN"),
    ADMIN_SELF_ACTION_FORBIDDEN(HttpStatus.FORBIDDEN, "ADMIN không được phép thay đổi trạng thái tài khoản của chính mình"),
    ADMIN_USER_ALREADY_BLOCKED(HttpStatus.CONFLICT, "Tài khoản người dùng đã bị khóa"),
    ADMIN_USER_ALREADY_ACTIVE(HttpStatus.CONFLICT, "Tài khoản người dùng đang hoạt động"),
    ADMIN_BLOCK_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "Lý do khóa tài khoản là bắt buộc"),
    SEARCH_HASHTAG_INVALID(HttpStatus.BAD_REQUEST, "Hashtag tìm kiếm không hợp lệ"),
    SEARCH_KEYWORD_REQUIRED(HttpStatus.BAD_REQUEST, "Từ khóa tìm kiếm không được để trống"),
    SEARCH_KEYWORD_TOO_LONG(HttpStatus.BAD_REQUEST, "Từ khóa tìm kiếm không được vượt quá 100 ký tự"),
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
    POST_CONTENT_OR_IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "Bài viết phải có nội dung hoặc ít nhất một ảnh"),
    POST_CONTENT_TOO_LONG(HttpStatus.BAD_REQUEST, "Nội dung bài viết không được vượt quá 500 ký tự"),
    POST_IMAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "Bài viết chỉ được tải lên tối đa 4 ảnh"),
    POST_IMAGE_FILE_EMPTY(HttpStatus.BAD_REQUEST, "Ảnh bài viết không được để trống"),
    POST_IMAGE_TOO_LARGE(HttpStatus.BAD_REQUEST, "Mỗi ảnh bài viết không được vượt quá 10 MB"),
    POST_IMAGE_EXTENSION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "Định dạng ảnh bài viết không được hỗ trợ"),
    POST_IMAGE_MIME_TYPE_INVALID(HttpStatus.BAD_REQUEST, "MIME type của ảnh bài viết không hợp lệ"),
    POST_IMAGE_SIGNATURE_INVALID(HttpStatus.BAD_REQUEST, "Nội dung tệp ảnh bài viết không hợp lệ"),
    POST_IMAGE_UPLOAD_FAILED(HttpStatus.BAD_GATEWAY, "Không thể tải ảnh bài viết lên hệ thống"),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết"),
    POST_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "Bài viết không khả dụng để thực hiện thao tác này"),
    POST_ALREADY_LIKED(HttpStatus.CONFLICT, "Bạn đã Like bài viết này"),
    POST_NOT_LIKED(HttpStatus.BAD_REQUEST, "Bạn chưa Like bài viết này"),
    POST_FORBIDDEN(HttpStatus.FORBIDDEN, "Bạn không có quyền chỉnh sửa bài viết này"),
    POST_EDIT_TIME_EXPIRED(HttpStatus.BAD_REQUEST, "Bài viết chỉ được chỉnh sửa trong vòng 15 phút sau khi đăng"),
    POST_CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, "Bài viết sau khi chỉnh sửa phải có nội dung hoặc ít nhất một ảnh"),
    POST_MEDIA_NOT_FOUND(HttpStatus.BAD_REQUEST, "Ảnh cần giữ lại không thuộc bài viết đang chỉnh sửa"),
    POST_HASHTAG_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "Bài viết chỉ được gắn tối đa 10 hashtag"),
    POST_HASHTAG_TOO_LONG(HttpStatus.BAD_REQUEST, "Mỗi hashtag không được vượt quá 100 ký tự"),
    POST_HASHTAG_INVALID(HttpStatus.BAD_REQUEST, "Hashtag chỉ được chứa chữ, số hoặc dấu gạch dưới"),
    COMMENT_CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, "Nội dung bình luận không được để trống"),
    COMMENT_CONTENT_TOO_LONG(HttpStatus.BAD_REQUEST, "Nội dung bình luận không được vượt quá 1000 ký tự"),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy bình luận"),
    COMMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "Bạn không có quyền xóa bình luận này"),
    FOLLOW_SELF_FORBIDDEN(HttpStatus.BAD_REQUEST, "Bạn không thể theo dõi chính mình"),
    FOLLOW_ALREADY_EXISTS(HttpStatus.CONFLICT, "Bạn đã theo dõi người dùng này"),
    FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy quan hệ theo dõi"),
    REPORT_DESCRIPTION_REQUIRED(HttpStatus.BAD_REQUEST, "Mô tả là bắt buộc khi lý do báo cáo là OTHER"),
    REPORT_DESCRIPTION_TOO_LONG(HttpStatus.BAD_REQUEST, "Mô tả báo cáo không được vượt quá 1000 ký tự"),
    REPORT_OWN_POST_FORBIDDEN(HttpStatus.FORBIDDEN, "Bạn không thể báo cáo bài viết của chính mình"),
    REPORT_ALREADY_PENDING(HttpStatus.CONFLICT, "Bạn đã có một báo cáo đang chờ xử lý cho bài viết này"),
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
