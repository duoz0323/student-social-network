package com.stu.edu.vn.backend.recommendation.repository;

/** Projection phẳng giúp native query phân trang và tính điểm mà không tải Entity/N+1. */
public interface StudentRecommendationProjection {
    Long getUserId();
    String getUsername();
    String getDisplayName();
    String getAvatarUrl();
    Long getSchoolId();
    String getSchoolName();
    String getSchoolShortName();
    Long getFacultyId();
    String getFacultyName();
    Long getMajorId();
    String getMajorName();
    Integer getEntryYear();
    Integer getSameSchool();
    Integer getSameFaculty();
    Integer getSameMajor();
    Integer getSameEntryYear();
    Integer getCommonInterestCount();
    Integer getMutualConnectionCount();
    Integer getMatchScore();
}
