package com.stu.edu.vn.backend.post.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.post.dto.request.PostLocationRequest;
import com.stu.edu.vn.backend.post.enums.LocationAction;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PostLocationValidatorTest {
    private final PostLocationValidator validator = new PostLocationValidator();

    @Test
    void trimsStringsAndConvertsBlankAddressToNullWithoutLowercasingPlaceId() {
        PostLocationRequest result = validator.validateAndNormalizeLocation(request(
                "  ChIJ-AbC  ", "  Đại học STU  ", "   ", "10.7382456", "106.6778123"));
        assertThat(result.placeId()).isEqualTo("ChIJ-AbC");
        assertThat(result.displayName()).isEqualTo("Đại học STU");
        assertThat(result.formattedAddress()).isNull();
    }

    @Test
    void rejectsMissingAndOutOfRangeFieldsWithDedicatedCodes() {
        assertCode(request(" ", "Tên", null, "10", "106"), ErrorCode.LOCATION_PLACE_ID_REQUIRED);
        assertCode(request("id", " ", null, "10", "106"), ErrorCode.LOCATION_DISPLAY_NAME_REQUIRED);
        assertCode(request("id", "Tên", null, "91", "106"), ErrorCode.LOCATION_LATITUDE_INVALID);
        assertCode(request("id", "Tên", null, "10", "181"), ErrorCode.LOCATION_LONGITUDE_INVALID);
        assertCode(request("id", "Tên", null, null, "106"), ErrorCode.LOCATION_COORDINATES_REQUIRED);
        assertCode(request("id", "Tên", null, "10", null), ErrorCode.LOCATION_COORDINATES_REQUIRED);
        assertCode(request("x".repeat(256), "Tên", null, "10", "106"), ErrorCode.LOCATION_FIELD_TOO_LONG);
    }

    @Test
    void enforcesUpdateActionPayloadMatrixAndNullDefaultsToKeep() {
        assertThat(validator.validateLocationUpdateAction(null, null)).isEqualTo(LocationAction.KEEP);
        assertThat(validator.validateLocationUpdateAction(LocationAction.REPLACE,
                request("id", "Tên", null, "10", "106"))).isEqualTo(LocationAction.REPLACE);
        assertThatThrownBy(() -> validator.validateLocationUpdateAction(LocationAction.REPLACE, null))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.LOCATION_REQUIRED_FOR_REPLACE));
        assertThatThrownBy(() -> validator.validateLocationUpdateAction(LocationAction.REMOVE,
                request("id", "Tên", null, "10", "106")))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.LOCATION_PAYLOAD_NOT_ALLOWED));
    }

    private void assertCode(PostLocationRequest request, ErrorCode code) {
        assertThatThrownBy(() -> validator.validateAndNormalizeLocation(request))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo(code));
    }

    private PostLocationRequest request(String placeId, String name, String address, String lat, String lng) {
        return new PostLocationRequest(placeId, name, address,
                lat == null ? null : new BigDecimal(lat), lng == null ? null : new BigDecimal(lng));
    }
}
