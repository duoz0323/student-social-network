package com.stu.edu.vn.backend.post.validation;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Chuẩn hóa hashtag về một giá trị duy nhất dùng thống nhất cho Create, Update, Search và Suggestion.
 */
@Component
public class HashtagNormalizer {

    private static final int MAX_HASHTAG_LENGTH = 100;
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("[\\p{Z}\\s]+", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern HASHTAG_PATTERN = Pattern.compile(
            "^[\\p{L}\\p{M}\\p{N}_]+(?: [\\p{L}\\p{M}\\p{N}_]+)*$"
    );

    public String normalizeOptional(String rawHashtag) {
        // Null hoặc raw chỉ có khoảng trắng biểu diễn field tùy chọn không có giá trị.
        if (rawHashtag == null || isOnlyUnicodeWhitespace(rawHashtag)) {
            return null;
        }
        return normalizeRequired(rawHashtag);
    }

    public String normalizeSuggestionKeyword(String keyword) {
        // Suggestion dùng đúng pipeline ghi dữ liệu; null/blank vẫn trả chuỗi rỗng để giữ contract từ khóa ngắn.
        String normalizedKeyword = normalizeOptional(keyword);
        return normalizedKeyword == null ? "" : normalizedKeyword;
    }

    private String normalizeRequired(String rawHashtag) {
        // Loại bỏ mọi dấu # vì hashtag được nhập ở field riêng và database không lưu ký tự trình bày này.
        String withoutHashes = rawHashtag.replace("#", "");
        String stripped = stripUnicodeWhitespace(withoutHashes);
        String collapsedWhitespace = WHITESPACE_PATTERN.matcher(stripped).replaceAll(" ");
        String normalizedHashtag = Normalizer.normalize(collapsedWhitespace, Normalizer.Form.NFC)
                .toLowerCase(Locale.ROOT);
        if (normalizedHashtag.isEmpty()) {
            throw new BusinessException(ErrorCode.POST_HASHTAG_INVALID);
        }
        if (normalizedHashtag.codePointCount(0, normalizedHashtag.length()) > MAX_HASHTAG_LENGTH) {
            throw new BusinessException(ErrorCode.POST_HASHTAG_TOO_LONG);
        }
        if (!HASHTAG_PATTERN.matcher(normalizedHashtag).matches()) {
            throw new BusinessException(ErrorCode.POST_HASHTAG_INVALID);
        }
        return normalizedHashtag;
    }

    private String stripUnicodeWhitespace(String value) {
        // Character.isSpaceChar bổ sung các separator như non-breaking space mà String.trim không xử lý.
        int start = 0;
        int end = value.length();
        while (start < end) {
            int codePoint = value.codePointAt(start);
            if (!isUnicodeWhitespace(codePoint)) {
                break;
            }
            start += Character.charCount(codePoint);
        }
        while (end > start) {
            int codePoint = value.codePointBefore(end);
            if (!isUnicodeWhitespace(codePoint)) {
                break;
            }
            end -= Character.charCount(codePoint);
        }
        return value.substring(start, end);
    }

    private boolean isUnicodeWhitespace(int codePoint) {
        return Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint);
    }

    private boolean isOnlyUnicodeWhitespace(String value) {
        return value.codePoints().allMatch(this::isUnicodeWhitespace);
    }
}
