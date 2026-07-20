package com.stu.edu.vn.backend.auth.mapper;

import com.stu.edu.vn.backend.auth.dto.LoginResponse;
import com.stu.edu.vn.backend.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Chuyển dữ liệu tài khoản sang DTO xác thực tối giản, không đưa thông tin liên hệ hoặc password hash ra API.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AuthMapper {

    LoginResponse.UserSummary toUserSummary(User user);
}
