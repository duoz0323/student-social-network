package com.stu.edu.vn.backend.common.controller;

import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint liveness tối giản để Render kiểm tra process mà không tạo truy vấn MySQL định kỳ. */
@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(Map.of("status", "UP"));
    }
}
