package com.stu.edu.vn.backend.report.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.report.dto.request.CreateProfileReportRequest;
import com.stu.edu.vn.backend.report.dto.response.CreateProfileReportResponse;
import com.stu.edu.vn.backend.report.service.ProfileReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** API để USER báo cáo trang cá nhân của một USER khác. */
@RestController
@RequestMapping("/api/v1/users/{userId}/profile-reports")
public class ProfileReportController {

    private final ProfileReportService profileReportService;

    public ProfileReportController(ProfileReportService profileReportService) {
        this.profileReportService = profileReportService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateProfileReportResponse>> create(
            @PathVariable Long userId,
            @Valid @RequestBody CreateProfileReportRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Gửi báo cáo trang cá nhân thành công",
                profileReportService.createProfileReport(userId, request)
        ));
    }
}
