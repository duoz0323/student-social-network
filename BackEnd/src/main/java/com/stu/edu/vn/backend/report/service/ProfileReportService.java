package com.stu.edu.vn.backend.report.service;

import com.stu.edu.vn.backend.report.dto.request.CreateProfileReportRequest;
import com.stu.edu.vn.backend.report.dto.response.CreateProfileReportResponse;

public interface ProfileReportService {
    CreateProfileReportResponse createProfileReport(Long reportedUserId, CreateProfileReportRequest request);
}
