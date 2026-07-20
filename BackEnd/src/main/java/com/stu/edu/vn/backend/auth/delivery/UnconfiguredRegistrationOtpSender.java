package com.stu.edu.vn.backend.auth.delivery;

import com.stu.edu.vn.backend.auth.enums.RegistrationType;
import org.springframework.stereotype.Component;

/**
 * Adapter an toàn khi chưa cấu hình email/SMS thật: báo thất bại rõ ràng, không log hay giả lập đã gửi.
 */
@Component
public class UnconfiguredRegistrationOtpSender implements RegistrationOtpSender {

    static final String PROVIDER_NOT_CONFIGURED = "DELIVERY_PROVIDER_NOT_CONFIGURED";

    @Override
    public OtpDeliveryResult send(RegistrationType type, String normalizedIdentifier, String rawOtp) {
        return OtpDeliveryResult.failed(PROVIDER_NOT_CONFIGURED);
    }
}
