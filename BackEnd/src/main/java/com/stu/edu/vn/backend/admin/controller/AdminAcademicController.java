package com.stu.edu.vn.backend.admin.controller;

import com.stu.edu.vn.backend.admin.dto.request.AdminAcademicNameRequest;
import com.stu.edu.vn.backend.admin.dto.request.AdminAcademicSchoolRequest;
import com.stu.edu.vn.backend.admin.dto.request.AdminAcademicStatusRequest;
import com.stu.edu.vn.backend.admin.dto.response.AdminFacultyResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminInterestResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminMajorResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminSchoolResponse;
import com.stu.edu.vn.backend.admin.service.AdminAcademicService;
import com.stu.edu.vn.backend.common.api.ApiResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API quản trị School → Faculty → Major và Interest Category, không cung cấp hard delete. */
@Validated
@RestController
@RequestMapping("/api/v1/admin/academic")
@RequiredArgsConstructor
public class AdminAcademicController {
    private final AdminAcademicService service;

    @GetMapping("/schools")
    public ResponseEntity<ApiResponse<PageResponse<AdminSchoolResponse>>> getSchools(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách trường quản trị thành công", service.getSchools(keyword, page, size)));
    }

    @PostMapping("/schools")
    public ResponseEntity<ApiResponse<AdminSchoolResponse>> createSchool(
            @RequestBody(required = false) AdminAcademicSchoolRequest request
    ) {
        AdminSchoolResponse response = service.createSchool(
                request == null ? null : request.name(), request == null ? null : request.shortName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo trường thành công", response));
    }

    @PutMapping("/schools/{schoolId}")
    public ResponseEntity<ApiResponse<AdminSchoolResponse>> updateSchool(
            @PathVariable Long schoolId,
            @RequestBody(required = false) AdminAcademicSchoolRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trường thành công", service.updateSchool(
                schoolId, request == null ? null : request.name(), request == null ? null : request.shortName())));
    }

    @PatchMapping("/schools/{schoolId}/status")
    public ResponseEntity<ApiResponse<AdminSchoolResponse>> changeSchoolStatus(
            @PathVariable Long schoolId,
            @RequestBody(required = false) AdminAcademicStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái trường thành công",
                service.changeSchoolStatus(schoolId, request == null ? null : request.status())));
    }

    @GetMapping("/schools/{schoolId}/faculties")
    public ResponseEntity<ApiResponse<PageResponse<AdminFacultyResponse>>> getFaculties(
            @PathVariable Long schoolId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách khoa quản trị thành công",
                service.getFaculties(schoolId, keyword, page, size)));
    }

    @PostMapping("/schools/{schoolId}/faculties")
    public ResponseEntity<ApiResponse<AdminFacultyResponse>> createFaculty(
            @PathVariable Long schoolId,
            @RequestBody(required = false) AdminAcademicNameRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo khoa thành công",
                service.createFaculty(schoolId, request == null ? null : request.name())));
    }

    @PutMapping("/faculties/{facultyId}")
    public ResponseEntity<ApiResponse<AdminFacultyResponse>> updateFaculty(
            @PathVariable Long facultyId,
            @RequestBody(required = false) AdminAcademicNameRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật khoa thành công",
                service.updateFaculty(facultyId, request == null ? null : request.name())));
    }

    @PatchMapping("/faculties/{facultyId}/status")
    public ResponseEntity<ApiResponse<AdminFacultyResponse>> changeFacultyStatus(
            @PathVariable Long facultyId,
            @RequestBody(required = false) AdminAcademicStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái khoa thành công",
                service.changeFacultyStatus(facultyId, request == null ? null : request.status())));
    }

    @GetMapping("/faculties/{facultyId}/majors")
    public ResponseEntity<ApiResponse<PageResponse<AdminMajorResponse>>> getMajors(
            @PathVariable Long facultyId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách ngành quản trị thành công",
                service.getMajors(facultyId, keyword, page, size)));
    }

    @PostMapping("/faculties/{facultyId}/majors")
    public ResponseEntity<ApiResponse<AdminMajorResponse>> createMajor(
            @PathVariable Long facultyId,
            @RequestBody(required = false) AdminAcademicNameRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo ngành thành công",
                service.createMajor(facultyId, request == null ? null : request.name())));
    }

    @PutMapping("/majors/{majorId}")
    public ResponseEntity<ApiResponse<AdminMajorResponse>> updateMajor(
            @PathVariable Long majorId,
            @RequestBody(required = false) AdminAcademicNameRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật ngành thành công",
                service.updateMajor(majorId, request == null ? null : request.name())));
    }

    @PatchMapping("/majors/{majorId}/status")
    public ResponseEntity<ApiResponse<AdminMajorResponse>> changeMajorStatus(
            @PathVariable Long majorId,
            @RequestBody(required = false) AdminAcademicStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái ngành thành công",
                service.changeMajorStatus(majorId, request == null ? null : request.status())));
    }

    @GetMapping("/interests")
    public ResponseEntity<ApiResponse<PageResponse<AdminInterestResponse>>> getInterests(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sở thích quản trị thành công",
                service.getInterests(keyword, page, size)));
    }

    @PostMapping("/interests")
    public ResponseEntity<ApiResponse<AdminInterestResponse>> createInterest(
            @RequestBody(required = false) AdminAcademicNameRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo sở thích thành công",
                service.createInterest(request == null ? null : request.name())));
    }

    @PutMapping("/interests/{interestId}")
    public ResponseEntity<ApiResponse<AdminInterestResponse>> updateInterest(
            @PathVariable Long interestId,
            @RequestBody(required = false) AdminAcademicNameRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật sở thích thành công",
                service.updateInterest(interestId, request == null ? null : request.name())));
    }

    @PatchMapping("/interests/{interestId}/status")
    public ResponseEntity<ApiResponse<AdminInterestResponse>> changeInterestStatus(
            @PathVariable Long interestId,
            @RequestBody(required = false) AdminAcademicStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái sở thích thành công",
                service.changeInterestStatus(interestId, request == null ? null : request.status())));
    }
}
