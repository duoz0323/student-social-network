package com.stu.edu.vn.backend.post.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.util.List;
import org.junit.jupiter.api.Test;

class HashtagNormalizerTest {

    private final HashtagNormalizer normalizer = new HashtagNormalizer();

    @Test
    void normalizeTrimsRemovesHashLowercasesAndRemovesDuplicates() {
        // Hashtag được chuẩn hóa trước khi lưu để tránh trùng dữ liệu.
        List<String> result = normalizer.normalize(List.of("  #SinhVien  ", "sinhvien", "HocTap"));

        assertThat(result).containsExactly("sinhvien", "hoctap");
    }

    @Test
    void normalizeAcceptsVietnameseHashtag() {
        // Regex dùng Unicode letter nên hashtag tiếng Việt hợp lệ.
        List<String> result = normalizer.normalize(List.of("#ĐồÁn_TốtNghiệp2026"));

        assertThat(result).containsExactly("đồán_tốtnghiệp2026");
    }

    @Test
    void normalizeRejectsWhitespaceOrSpecialCharactersInsideHashtag() {
        // Hashtag không được chứa khoảng trắng hoặc ký tự đặc biệt ngoài dấu gạch dưới.
        assertThatThrownBy(() -> normalizer.normalize(List.of("hoc tap")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_HASHTAG_INVALID);

        assertThatThrownBy(() -> normalizer.normalize(List.of("hoc-tap")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_HASHTAG_INVALID);
    }

    @Test
    void normalizeRejectsMoreThanTenUniqueHashtags() {
        // Giới hạn 10 hashtag được tính sau khi chuẩn hóa và loại trùng.
        List<String> hashtags = List.of("h1", "h2", "h3", "h4", "h5", "h6", "h7", "h8", "h9", "h10", "h11");

        assertThatThrownBy(() -> normalizer.normalize(hashtags))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_HASHTAG_LIMIT_EXCEEDED);
    }

    @Test
    void normalizeRejectsHashtagLongerThanOneHundredCharacters() {
        // Mỗi hashtag khớp độ dài cột normalized_name/display_name trong database.
        String longHashtag = "a".repeat(101);

        assertThatThrownBy(() -> normalizer.normalize(List.of(longHashtag)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.POST_HASHTAG_TOO_LONG);
    }

    @Test
    void normalizeAcceptsExactlyOneHundredCharacters() {
        // Độ dài 100 ký tự là hợp lệ theo schema.
        String hashtag = "a".repeat(100);

        assertThat(normalizer.normalize(List.of(hashtag))).containsExactly(hashtag);
    }
}
