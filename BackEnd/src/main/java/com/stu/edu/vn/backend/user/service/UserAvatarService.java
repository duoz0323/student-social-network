package com.stu.edu.vn.backend.user.service;

import com.stu.edu.vn.backend.user.dto.response.AvatarResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service quản lý avatar của người dùng hiện tại trong cả onboarding và sau onboarding.
 */
public interface UserAvatarService {

    AvatarResponse uploadMyAvatar(MultipartFile file);

    AvatarResponse deleteMyAvatar();
}
