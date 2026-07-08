package com.stu.edu.vn.backend.post.validation;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Chuẩn hóa hashtag bài viết về dạng lưu database: lowercase, không có ký tự # và không trùng.
 */
@Component
public class HashtagNormalizer {

    private static final int MAX_HASHTAG_COUNT = 10;
    private static final int MAX_HASHTAG_LENGTH = 100;
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("^[\\p{L}\\p{N}_]+$");

    public List<String> normalize(List<String> hashtags) {
        // Null hoặc list rỗng nghĩa là bài viết không gắn hashtag.
        if (hashtags == null || hashtags.isEmpty()) {
            return List.of();
        }
        Set<String> normalizedHashtags = new LinkedHashSet<>();
        for (String hashtag : hashtags) {
            String normalizedHashtag = normalizeOne(hashtag);
            normalizedHashtags.add(normalizedHashtag);
        }
        if (normalizedHashtags.size() > MAX_HASHTAG_COUNT) {
            throw new BusinessException(ErrorCode.POST_HASHTAG_LIMIT_EXCEEDED);
        }
        return new ArrayList<>(normalizedHashtags);
    }

    private String normalizeOne(String hashtag) {
        // Hashtag được trim, bỏ toàn bộ dấu # ở đầu và chuyển lowercase để chống trùng.
        if (hashtag == null) {
            throw new BusinessException(ErrorCode.POST_HASHTAG_INVALID);
        }
        String normalizedHashtag = removeLeadingHashes(hashtag.trim()).toLowerCase(Locale.ROOT);
        if (normalizedHashtag.isEmpty()) {
            throw new BusinessException(ErrorCode.POST_HASHTAG_INVALID);
        }
        if (normalizedHashtag.length() > MAX_HASHTAG_LENGTH) {
            throw new BusinessException(ErrorCode.POST_HASHTAG_TOO_LONG);
        }
        if (!HASHTAG_PATTERN.matcher(normalizedHashtag).matches()) {
            throw new BusinessException(ErrorCode.POST_HASHTAG_INVALID);
        }
        return normalizedHashtag;
    }

    private String removeLeadingHashes(String value) {
        // Chỉ bỏ ký tự # ở đầu, không cho phép # ở giữa vì đó là ký tự đặc biệt không hợp lệ.
        int index = 0;
        while (index < value.length() && value.charAt(index) == '#') {
            index++;
        }
        return value.substring(index);
    }
}
