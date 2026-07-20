package com.stu.edu.vn.backend.security;

import com.stu.edu.vn.backend.common.api.ErrorResponse;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Ghi response lỗi bảo mật thống nhất với GlobalExceptionHandler.
 */
@Component
@RequiredArgsConstructor
public class SecurityErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public void write(HttpServletResponse response, HttpServletRequest request, ErrorCode errorCode)
            throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        objectMapper.writeValue(
                response.getWriter(),
                ErrorResponse.of(
                        errorCode.name(),
                        errorCode.getDefaultMessage(),
                        errorCode.getHttpStatus().value(),
                        request.getRequestURI()
                )
        );
    }
}
