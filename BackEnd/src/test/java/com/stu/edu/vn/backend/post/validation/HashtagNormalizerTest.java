package com.stu.edu.vn.backend.post.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

class HashtagNormalizerTest {

    private final HashtagNormalizer normalizer = new HashtagNormalizer();

    @Test
    void normalizeRemovesEveryHashTrimsCollapsesWhitespaceAndLowercases() {
        // Field hashtag không lưu ký tự # và chỉ giữ đúng một space giữa các từ.
        assertThat(normalizer.normalizeOptional(" \u00A0#Sinh   #Viên\t "))
                .isEqualTo("sinh viên");
    }

    @Test
    void normalizeAcceptsVietnameseMultipleWordsAndNfcUnicode() {
        // Chuỗi decomposed được đưa về NFC để cùng một hashtag có đúng một normalized_name.
        assertThat(normalizer.normalizeOptional("Đồ a\u0301n Tốt Nghiệp 2026"))
                .isEqualTo("đồ án tốt nghiệp 2026");
    }

    @Test
    void normalizeTreatsNullAndUnicodeWhitespaceAsAbsent() {
        assertThat(normalizer.normalizeOptional(null)).isNull();
        assertThat(normalizer.normalizeOptional(" \t\u00A0\n")).isNull();
    }

    @Test
    void normalizeRejectsNonBlankInputThatBecomesEmptyAndSpecialCharacters() {
        assertError("###", ErrorCode.POST_HASHTAG_INVALID);
        assertError("hoc-tap", ErrorCode.POST_HASHTAG_INVALID);
    }

    @Test
    void normalizeChecksMaximumLengthAfterNormalizationByCodePoint() {
        String exactlyOneHundred = "a".repeat(99) + "á";
        assertThat(normalizer.normalizeOptional(exactlyOneHundred)).isEqualTo(exactlyOneHundred);
        assertError(exactlyOneHundred + "b", ErrorCode.POST_HASHTAG_TOO_LONG);
    }

    private void assertError(String rawHashtag, ErrorCode errorCode) {
        assertThatThrownBy(() -> normalizer.normalizeOptional(rawHashtag))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }
}
