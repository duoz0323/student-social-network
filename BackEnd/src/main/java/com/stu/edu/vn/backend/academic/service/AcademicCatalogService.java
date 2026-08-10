package com.stu.edu.vn.backend.academic.service;

import com.stu.edu.vn.backend.academic.dto.response.AcademicItemResponse;
import com.stu.edu.vn.backend.academic.dto.response.InterestResponse;
import com.stu.edu.vn.backend.academic.dto.response.SchoolResponse;
import java.util.List;

/** Cung cấp master data ACTIVE cho autocomplete và lựa chọn sở thích. */
public interface AcademicCatalogService {
    List<SchoolResponse> searchSchools(String keyword, Integer limit);

    List<AcademicItemResponse> searchFaculties(Long schoolId, String keyword, Integer limit);

    List<AcademicItemResponse> searchMajors(Long facultyId, String keyword, Integer limit);

    List<InterestResponse> getInterests();
}
