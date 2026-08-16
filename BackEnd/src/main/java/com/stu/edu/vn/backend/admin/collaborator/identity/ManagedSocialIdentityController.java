package com.stu.edu.vn.backend.admin.collaborator.identity;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
public class ManagedSocialIdentityController {
    private final ManagedSocialIdentityService service;

    @GetMapping("/api/v1/admin/collaborator/social-identity")
    @PreAuthorize("hasAuthority('COLLABORATOR_DASHBOARD_VIEW')")
    public ResponseEntity<ApiResponse<ManagedSocialIdentityResponse>> getCurrent() {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh tính xã hội thành công", service.getCurrent()));
    }

    @PostMapping("/api/v1/admin/admins/{adminId}/social-identity")
    @PreAuthorize("@bootstrapAdminAuthorization.isCurrentBootstrapAdmin()")
    public ResponseEntity<ApiResponse<ManagedSocialIdentityResponse>> create(
            @PathVariable @Positive Long adminId,
            @Valid @RequestBody CreateManagedSocialIdentityRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo danh tính xã hội thành công", service.create(adminId, request)));
    }
}
