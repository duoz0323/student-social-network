package com.stu.edu.vn.backend.admin.collaborator.identity;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.storage.CloudinaryStorageService;
import com.stu.edu.vn.backend.storage.CloudinaryUploadResult;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.service.impl.UserAvatarFileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

/** Upload avatar cho Managed Identity bằng cùng validation và storage của hồ sơ USER. */
@Service
@Slf4j
@RequiredArgsConstructor
public class ManagedSocialIdentityAvatarService {
    private final CollaboratorSocialIdentityResolver identityResolver;
    private final CurrentUserProvider currentUserProvider;
    private final UserProfileRepository profileRepository;
    private final CloudinaryStorageService storageService;
    private final UserAvatarFileValidator fileValidator;
    private final TransactionTemplate transactionTemplate;

    public ManagedSocialIdentityResponse upload(MultipartFile file) {
        fileValidator.validate(file);
        User socialUser = identityResolver.resolveActive(currentUserProvider.getCurrentUserId());
        CloudinaryUploadResult uploaded = storageService.uploadAvatar(file);
        try {
            AvatarUpdate update = transactionTemplate.execute(status -> updateProfile(socialUser, uploaded));
            deleteBestEffort(update.oldPublicId(), "Không thể xóa avatar Managed Identity cũ");
            return update.response();
        } catch (RuntimeException exception) {
            deleteBestEffort(uploaded.publicId(), "Không thể cleanup avatar Managed Identity mới");
            throw exception;
        }
    }

    private AvatarUpdate updateProfile(User socialUser, CloudinaryUploadResult uploaded) {
        UserProfile profile = profileRepository.findByIdForUpdate(socialUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        String oldPublicId = profile.getAvatarPublicId();
        profile.setAvatarUrl(uploaded.url());
        profile.setAvatarPublicId(uploaded.publicId());
        profileRepository.saveAndFlush(profile);
        return new AvatarUpdate(oldPublicId, new ManagedSocialIdentityResponse(socialUser.getId(),
                profile.getUsername(), profile.getDisplayName(), profile.getAvatarUrl(), profile.getBio(), true));
    }

    private void deleteBestEffort(String publicId, String message) {
        try {
            storageService.deleteImage(publicId);
        } catch (BusinessException exception) {
            // Không log public_id để tránh lộ metadata lưu trữ.
            log.warn(message);
        }
    }

    private record AvatarUpdate(String oldPublicId, ManagedSocialIdentityResponse response) { }
}
