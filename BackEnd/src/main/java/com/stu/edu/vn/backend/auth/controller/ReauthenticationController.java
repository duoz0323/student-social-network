package com.stu.edu.vn.backend.auth.controller;

import com.stu.edu.vn.backend.auth.dto.ReauthenticationRequest;
import com.stu.edu.vn.backend.auth.dto.ReauthenticationResponse;
import com.stu.edu.vn.backend.auth.service.ReauthenticationService;
import com.stu.edu.vn.backend.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** HTTP adapter cho xác thực lại user hiện tại trước thao tác tài khoản nhạy cảm. */
@RestController
@RequestMapping("/api/v1/auth")
public class ReauthenticationController {

    private final ReauthenticationService service;

    public ReauthenticationController(ReauthenticationService service) {
        this.service = service;
    }

    @PostMapping("/reauthenticate")
    public ResponseEntity<ApiResponse<ReauthenticationResponse>> reauthenticate(
            @Valid @RequestBody ReauthenticationRequest request
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(ApiResponse.success("Xác thực lại thành công", service.reauthenticate(request)));
    }
}
