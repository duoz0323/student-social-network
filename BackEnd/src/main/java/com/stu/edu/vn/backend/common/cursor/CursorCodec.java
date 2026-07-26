package com.stu.edu.vn.backend.common.cursor;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mã hóa cursor thành Base64URL opaque và gom mọi lỗi kỹ thuật về INVALID_CURSOR.
 */
@Component
@RequiredArgsConstructor
public class CursorCodec {
    private final ObjectMapper objectMapper;

    public String encode(Object cursor) {
        try {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(cursor));
        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    public <T> T decode(String encodedCursor, Class<T> cursorType) {
        if (encodedCursor == null) {
            return null;
        }
        if (encodedCursor.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_CURSOR);
        }
        try {
            byte[] json = Base64.getUrlDecoder().decode(encodedCursor.getBytes(StandardCharsets.US_ASCII));
            return objectMapper.readValue(json, cursorType);
        } catch (IllegalArgumentException | JacksonException exception) {
            throw new BusinessException(ErrorCode.INVALID_CURSOR);
        }
    }
}
