package com.stu.edu.vn.backend.auth.delivery;

/** Kết quả chuẩn hóa, không chứa payload hoặc exception message của nhà cung cấp. */
public record OtpDeliveryResult(OtpDeliveryOutcome outcome, String failureCode) {

    public static OtpDeliveryResult sent() {
        return new OtpDeliveryResult(OtpDeliveryOutcome.SENT, null);
    }

    public static OtpDeliveryResult failed(String failureCode) {
        return new OtpDeliveryResult(OtpDeliveryOutcome.FAILED, failureCode);
    }

    public static OtpDeliveryResult unknown() {
        return new OtpDeliveryResult(OtpDeliveryOutcome.UNKNOWN, null);
    }
}
