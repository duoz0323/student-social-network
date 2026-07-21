package com.stu.edu.vn.backend.auth.delivery;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.auth.config.OtpDeliveryProperties;
import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class ProviderRegistrationOtpSenderTest {
    @Test
    void returnsFailureWhenBrevoIsNotConfigured() {
        ProviderRegistrationOtpSender sender = new ProviderRegistrationOtpSender(
                new OtpDeliveryProperties(), JsonMapper.builder().build());
        OtpDeliveryResult result = sender.send(RegistrationType.EMAIL, "student@example.com", "123456");
        assertThat(result.outcome()).isEqualTo(OtpDeliveryOutcome.FAILED);
        assertThat(result.failureCode()).isEqualTo("BREVO_NOT_CONFIGURED");
    }

    @Test
    void rejectsMissingEmail() {
        ProviderRegistrationOtpSender sender = new ProviderRegistrationOtpSender(
                new OtpDeliveryProperties(), JsonMapper.builder().build());
        assertThat(sender.send(RegistrationType.EMAIL, null, "123456").outcome())
                .isEqualTo(OtpDeliveryOutcome.FAILED);
    }

    @Test
    void emailContentContainsOtpExpiryAndSecurityWarning() {
        ProviderRegistrationOtpSender sender = new ProviderRegistrationOtpSender(
                new OtpDeliveryProperties(), JsonMapper.builder().build());

        assertThat(sender.emailContent("123456"))
                .contains("123456")
                .contains("10 phút")
                .contains("Tuyệt đối không chia sẻ mã này cho bất kỳ ai")
                .contains("UniShare không bao giờ yêu cầu bạn cung cấp mã OTP");
    }
}
