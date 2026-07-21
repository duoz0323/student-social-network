package com.stu.edu.vn.backend.common.exception;

import com.stu.edu.vn.backend.common.api.ErrorResponse;
import com.stu.edu.vn.backend.common.api.FieldErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * Xử lý lỗi tập trung để API không trả stack trace hoặc chi tiết nội bộ cho Client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = exception.getErrorCode();
        ErrorResponse response = ErrorResponse.of(
                errorCode.name(),
                exception.getMessage(),
                errorCode.getHttpStatus().value(),
                request.getRequestURI()
        );
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<FieldErrorDetail> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();
        ErrorResponse response = ErrorResponse.validation(
                ErrorCode.VALIDATION_ERROR.getDefaultMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<FieldErrorDetail> fieldErrors = exception.getConstraintViolations()
                .stream()
                .map(violation -> new FieldErrorDetail(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ))
                .toList();
        ErrorResponse response = ErrorResponse.validation(
                ErrorCode.VALIDATION_ERROR.getDefaultMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
                ErrorCode.VALIDATION_ERROR.name(),
                "Dữ liệu vi phạm ràng buộc lưu trữ",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
                ErrorCode.VALIDATION_ERROR.name(),
                ErrorCode.VALIDATION_ERROR.getDefaultMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestPart(HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
                ErrorCode.AVATAR_FILE_REQUIRED.name(),
                ErrorCode.AVATAR_FILE_REQUIRED.getDefaultMessage(),
                ErrorCode.AVATAR_FILE_REQUIRED.getHttpStatus().value(),
                request.getRequestURI()
        );
        return ResponseEntity.status(ErrorCode.AVATAR_FILE_REQUIRED.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
                ErrorCode.AVATAR_FILE_TOO_LARGE.name(),
                ErrorCode.AVATAR_FILE_TOO_LARGE.getDefaultMessage(),
                ErrorCode.AVATAR_FILE_TOO_LARGE.getHttpStatus().value(),
                request.getRequestURI()
        );
        return ResponseEntity.status(ErrorCode.AVATAR_FILE_TOO_LARGE.getHttpStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INTERNAL_ERROR.name(),
                ErrorCode.INTERNAL_ERROR.getDefaultMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
