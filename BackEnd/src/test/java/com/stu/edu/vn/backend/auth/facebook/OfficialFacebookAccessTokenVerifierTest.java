package com.stu.edu.vn.backend.auth.facebook;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stu.edu.vn.backend.auth.config.FacebookAuthProperties;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.time.Clock;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class OfficialFacebookAccessTokenVerifierTest {
    @Test
    void blankTokenIsRejectedBeforeAnyHttpCall() {
        var verifier = new OfficialFacebookAccessTokenVerifier(new FacebookAuthProperties(), new ObjectMapper(), Clock.systemUTC());
        assertThatThrownBy(() -> verifier.verify("  "))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode())
                .isEqualTo(ErrorCode.AUTH_FACEBOOK_TOKEN_REQUIRED);
    }
}
