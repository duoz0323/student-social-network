package com.stu.edu.vn.backend.auth.dto;

import com.stu.edu.vn.backend.auth.enums.AuthMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationMethod;
import com.stu.edu.vn.backend.auth.enums.ReauthenticationScope;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Request xác thực lại không nhận userId và chỉ nhận đúng credential của method đã chọn. */
public record ReauthenticationRequest(
        @NotNull(message = "Phương thức xác thực lại không được để trống")
        ReauthenticationMethod method,
        @NotNull(message = "Mục đích xác thực lại không được để trống")
        ReauthenticationScope purpose,
        @NotNull(message = "Phương thức đích không được để trống")
        AuthMethod targetMethod,
        @Size(max = 72, message = "Mật khẩu không hợp lệ")
        String password,
        @Size(max = 8192, message = "Provider credential không hợp lệ")
        @Pattern(regexp = "^\\S*$", message = "Provider credential không hợp lệ")
        String providerCredential
) {

    @AssertTrue(message = "Credential không phù hợp với phương thức xác thực lại")
    public boolean isCredentialSelectionValid() {
        if (method == null) {
            return true;
        }
        boolean hasPassword = password != null && !password.isBlank();
        boolean hasProviderCredential = providerCredential != null && !providerCredential.isBlank();
        return method == ReauthenticationMethod.PASSWORD
                ? hasPassword && !hasProviderCredential
                : !hasPassword && hasProviderCredential;
    }
}
