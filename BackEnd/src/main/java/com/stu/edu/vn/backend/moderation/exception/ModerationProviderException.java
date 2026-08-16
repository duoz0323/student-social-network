package com.stu.edu.vn.backend.moderation.exception;

/** Lỗi kỹ thuật kín của provider; service sẽ map thành lỗi nghiệp vụ fail-closed ổn định. */
public class ModerationProviderException extends RuntimeException {

    public ModerationProviderException(String message) {
        super(message);
    }

    public ModerationProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
