package com.stu.edu.vn.backend.user.service.impl;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.storage.CloudinaryStorageService;
import com.stu.edu.vn.backend.storage.CloudinaryUploadResult;
import com.stu.edu.vn.backend.user.dto.response.AvatarResponse;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.service.UserAvatarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * Xử lý upload/xóa avatar, tách thao tác Cloudinary khỏi transaction database bằng cleanup bù trừ.
 */
@Service
public class UserAvatarServiceImpl implements UserAvatarService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAvatarServiceImpl.class);

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final CloudinaryStorageService cloudinaryStorageService;
    private final UserAvatarFileValidator fileValidator;
    private final TransactionTemplate transactionTemplate;

    public UserAvatarServiceImpl(
            CurrentUserProvider currentUserProvider,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            CloudinaryStorageService cloudinaryStorageService,
            UserAvatarFileValidator fileValidator,
            TransactionTemplate transactionTemplate
    ) {
        this.currentUserProvider = currentUserProvider;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.cloudinaryStorageService = cloudinaryStorageService;
        this.fileValidator = fileValidator;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public AvatarResponse uploadMyAvatar(MultipartFile file) {
        fileValidator.validate(file);
        Long userId = currentUserProvider.getCurrentUserId();
        ensureActiveUser(userId);

        CloudinaryUploadResult newAvatar = cloudinaryStorageService.uploadAvatar(file);
        try {
            AvatarUpdateResult updateResult = transactionTemplate.execute(status -> updateAvatarInDatabase(userId, newAvatar));
            deleteOldAvatarAfterDatabaseSuccess(updateResult.oldAvatarPublicId());
            return new AvatarResponse(updateResult.avatarUrl(), updateResult.profileCompleted());
        } catch (RuntimeException exception) {
            cleanupNewAvatarAfterDatabaseFailure(newAvatar.publicId());
            throw exception;
        }
    }

    @Override
    public AvatarResponse deleteMyAvatar() {
        Long userId = currentUserProvider.getCurrentUserId();
        ensureActiveUser(userId);

        AvatarUpdateResult updateResult = transactionTemplate.execute(status -> clearAvatarInDatabase(userId));
        deleteOldAvatarAfterDatabaseSuccess(updateResult.oldAvatarPublicId());
        return new AvatarResponse(null, updateResult.profileCompleted());
    }

    private AvatarUpdateResult updateAvatarInDatabase(Long userId, CloudinaryUploadResult newAvatar) {
        UserProfile profile = findProfile(userId);
        String oldAvatarPublicId = profile.getAvatarPublicId();
        profile.setAvatarUrl(newAvatar.url());
        profile.setAvatarPublicId(newAvatar.publicId());
        userProfileRepository.saveAndFlush(profile);
        return new AvatarUpdateResult(profile.getAvatarUrl(), oldAvatarPublicId, profile.getProfileCompletedAt() != null);
    }

    private AvatarUpdateResult clearAvatarInDatabase(Long userId) {
        UserProfile profile = findProfile(userId);
        String oldAvatarPublicId = profile.getAvatarPublicId();
        profile.setAvatarUrl(null);
        profile.setAvatarPublicId(null);
        userProfileRepository.saveAndFlush(profile);
        return new AvatarUpdateResult(null, oldAvatarPublicId, profile.getProfileCompletedAt() != null);
    }

    private void ensureActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }
    }

    private UserProfile findProfile(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
    }

    private void deleteOldAvatarAfterDatabaseSuccess(String oldAvatarPublicId) {
        try {
            cloudinaryStorageService.deleteImage(oldAvatarPublicId);
        } catch (BusinessException exception) {
            // Không rollback DB khi xóa file cũ thất bại; file rác có thể được cleanup sau.
            LOGGER.warn("Không thể xóa avatar cũ trên Cloudinary sau khi database đã cập nhật");
        }
    }

    private void cleanupNewAvatarAfterDatabaseFailure(String newAvatarPublicId) {
        try {
            cloudinaryStorageService.deleteImage(newAvatarPublicId);
        } catch (BusinessException exception) {
            // Không log public_id hoặc secret để tránh lộ metadata lưu trữ.
            LOGGER.warn("Không thể cleanup avatar mới sau khi database cập nhật thất bại");
        }
    }

    private record AvatarUpdateResult(
            String avatarUrl,
            String oldAvatarPublicId,
            boolean profileCompleted
    ) {
    }
}
