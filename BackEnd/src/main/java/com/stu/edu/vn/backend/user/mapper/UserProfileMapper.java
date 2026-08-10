package com.stu.edu.vn.backend.user.mapper;

import com.stu.edu.vn.backend.user.dto.response.CompleteOnboardingResponse;
import com.stu.edu.vn.backend.user.dto.response.OnboardingStatusResponse;
import com.stu.edu.vn.backend.user.dto.response.UserProfileResponse;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Chuyển Entity hồ sơ sang DTO phản hồi; không để Service tự ghép dữ liệu trả về Client.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserProfileMapper {

    @Mapping(target = "profileCompleted", expression = "java(profile.isCompleted())")
    UserProfileResponse toUserProfileResponse(UserProfile profile);

    @Mapping(target = "profileCompleted", expression = "java(profile.isCompleted())")
    @Mapping(target = "nextStep", source = "nextStep")
    OnboardingStatusResponse toOnboardingStatusResponse(UserProfile profile, String nextStep);

    @Mapping(target = "profileCompleted", constant = "true")
    @Mapping(target = "nextStep", source = "nextStep")
    CompleteOnboardingResponse toCompleteOnboardingResponse(UserProfile profile, String nextStep);
}
