package com.stu.edu.vn.backend.admin.service;

import com.stu.edu.vn.backend.academic.enums.AcademicStatus;
import com.stu.edu.vn.backend.admin.dto.response.AdminFacultyResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminInterestResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminMajorResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminSchoolResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;

/** Use case quản trị master data học thuật, chỉ thay đổi dữ liệu được chỉ định và không hard delete. */
public interface AdminAcademicService {
    PageResponse<AdminSchoolResponse> getSchools(String keyword, int page, int size);
    AdminSchoolResponse createSchool(String name, String shortName);
    AdminSchoolResponse updateSchool(Long schoolId, String name, String shortName);
    AdminSchoolResponse changeSchoolStatus(Long schoolId, AcademicStatus status);

    PageResponse<AdminFacultyResponse> getFaculties(Long schoolId, String keyword, int page, int size);
    AdminFacultyResponse createFaculty(Long schoolId, String name);
    AdminFacultyResponse updateFaculty(Long facultyId, String name);
    AdminFacultyResponse changeFacultyStatus(Long facultyId, AcademicStatus status);

    PageResponse<AdminMajorResponse> getMajors(Long facultyId, String keyword, int page, int size);
    AdminMajorResponse createMajor(Long facultyId, String name);
    AdminMajorResponse updateMajor(Long majorId, String name);
    AdminMajorResponse changeMajorStatus(Long majorId, AcademicStatus status);

    PageResponse<AdminInterestResponse> getInterests(String keyword, int page, int size);
    AdminInterestResponse createInterest(String name);
    AdminInterestResponse updateInterest(Long interestId, String name);
    AdminInterestResponse changeInterestStatus(Long interestId, AcademicStatus status);
}
