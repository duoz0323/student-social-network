package com.stu.edu.vn.backend.academic.dto.response;

/** Trường học tối giản dùng cho autocomplete và hiển thị profile. */
public record SchoolResponse(Long id, String name, String shortName) {
}
