package com.stu.edu.vn.backend.user.controller;

import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.user.dto.response.AccountStandingResponse;
import com.stu.edu.vn.backend.user.service.AccountStandingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** API chỉ đọc Account Standing của chính tài khoản lấy từ JWT. */
@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountStandingController {
    private final AccountStandingService accountStandingService;

    @GetMapping("/standing")
    public ApiResponse<AccountStandingResponse> getStanding() {
        return ApiResponse.success("Đọc trạng thái tài khoản thành công", accountStandingService.getCurrentStanding());
    }
}
