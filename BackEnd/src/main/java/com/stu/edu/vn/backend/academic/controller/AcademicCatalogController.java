package com.stu.edu.vn.backend.academic.controller;

import com.stu.edu.vn.backend.academic.dto.response.AcademicItemResponse;
import com.stu.edu.vn.backend.academic.dto.response.InterestResponse;
import com.stu.edu.vn.backend.academic.dto.response.SchoolResponse;
import com.stu.edu.vn.backend.academic.service.AcademicCatalogService;
import com.stu.edu.vn.backend.common.api.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Read-only API master data; chưa có chức năng quản trị trường, khoa, ngành hoặc sở thích. */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AcademicCatalogController {
    private final AcademicCatalogService academicCatalogService;

    @GetMapping("/academic/schools")
    public ResponseEntity<ApiResponse<List<SchoolResponse>>> searchSchools(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách trường thành công",
                academicCatalogService.searchSchools(keyword, limit)
        ));
    }

    @GetMapping("/academic/schools/{schoolId}/faculties")
    public ResponseEntity<ApiResponse<List<AcademicItemResponse>>> searchFaculties(
            @PathVariable Long schoolId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách khoa thành công",
                academicCatalogService.searchFaculties(schoolId, keyword, limit)
        ));
    }

    @GetMapping("/academic/faculties/{facultyId}/majors")
    public ResponseEntity<ApiResponse<List<AcademicItemResponse>>> searchMajors(
            @PathVariable Long facultyId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách ngành thành công",
                academicCatalogService.searchMajors(facultyId, keyword, limit)
        ));
    }

    @GetMapping("/interests")
    public ResponseEntity<ApiResponse<List<InterestResponse>>> getInterests() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách sở thích thành công",
                academicCatalogService.getInterests()
        ));
    }
}
