package com.stu.edu.vn.backend.admin.service.impl;

import com.stu.edu.vn.backend.admin.dto.request.AdminBlockUserRequest;
import com.stu.edu.vn.backend.admin.dto.request.AdminUpdateUserProfileRequest;
import com.stu.edu.vn.backend.admin.dto.response.AdminUserDetailResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminUserListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminUserStatusResponse;
import com.stu.edu.vn.backend.admin.entity.AccountStatusHistory;
import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminAvatarAction;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.mapper.AdminUserMapper;
import com.stu.edu.vn.backend.admin.repository.AccountStatusHistoryRepository;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.admin.repository.AdminUserDetailProjection;
import com.stu.edu.vn.backend.admin.repository.AdminUserRepository;
import com.stu.edu.vn.backend.admin.service.AdminUserService;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.util.LikePatternEscaper;
import com.stu.edu.vn.backend.auth.repository.RefreshTokenRepository;
import com.stu.edu.vn.backend.notification.service.NotificationService;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.service.impl.UserProfileValidationSupport;
import com.stu.edu.vn.backend.user.service.impl.UserAvatarFileValidator;
import com.stu.edu.vn.backend.storage.CloudinaryStorageService;
import com.stu.edu.vn.backend.storage.CloudinaryUploadResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Triển khai truy vấn và transaction thay đổi trạng thái tài khoản USER dành cho ADMIN.
 */
@Service
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private static final int MAX_KEYWORD_LENGTH = 100;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String UNBLOCK_REASON = "ADMIN_UNBLOCK";

    private final AdminUserRepository adminUserRepository;
    private final AdminUserMapper adminUserMapper;
    private final CurrentUserProvider currentUserProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccountStatusHistoryRepository accountStatusHistoryRepository;
    private final AdminActionRepository adminActionRepository;
    private final Clock clock;
    private final EntityManager entityManager;
    private final NotificationService notificationService;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileValidationSupport profileValidationSupport;
    private final CloudinaryStorageService cloudinaryStorageService;
    private final UserAvatarFileValidator avatarFileValidator;
    private final TransactionTemplate transactionTemplate;

    public AdminUserServiceImpl(
            AdminUserRepository adminUserRepository,
            AdminUserMapper adminUserMapper,
            CurrentUserProvider currentUserProvider,
            RefreshTokenRepository refreshTokenRepository,
            AccountStatusHistoryRepository accountStatusHistoryRepository,
            AdminActionRepository adminActionRepository,
            Clock clock,
            EntityManager entityManager,
            NotificationService notificationService,
            UserProfileRepository userProfileRepository,
            UserProfileValidationSupport profileValidationSupport,
            CloudinaryStorageService cloudinaryStorageService,
            UserAvatarFileValidator avatarFileValidator,
            TransactionTemplate transactionTemplate
    ) {
        this.adminUserRepository = adminUserRepository;
        this.adminUserMapper = adminUserMapper;
        this.currentUserProvider = currentUserProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.accountStatusHistoryRepository = accountStatusHistoryRepository;
        this.adminActionRepository = adminActionRepository;
        this.clock = clock;
        this.entityManager = entityManager;
        this.notificationService = notificationService;
        this.userProfileRepository = userProfileRepository;
        this.profileValidationSupport = profileValidationSupport;
        this.cloudinaryStorageService = cloudinaryStorageService;
        this.avatarFileValidator = avatarFileValidator;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserListItemResponse> getUsers(
            String keyword,
            UserStatus status,
            int page,
            int size
    ) {
        validatePagination(page, size);
        String escapedKeyword = normalizeAndEscapeOptionalKeyword(keyword);
        String statusValue = status == null ? null : status.name();

        // Page.map chỉ ánh xạ projection đã có, không phát sinh truy vấn quan hệ bổ sung.
        return PageResponse.from(adminUserRepository
                .findManagedUsers(escapedKeyword, statusValue, PageRequest.of(page, size))
                .map(adminUserMapper::toListItem));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUserDetail(Long userId) {
        AdminUserDetailProjection target = adminUserRepository.findManagedUserDetail(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_USER_NOT_FOUND));

        // Query vẫn lấy role để phân biệt target ADMIN với tài khoản hoàn toàn không tồn tại.
        if (!UserRole.USER.name().equals(target.getRole())) {
            throw new BusinessException(ErrorCode.ADMIN_USER_MANAGEMENT_FORBIDDEN);
        }
        return adminUserMapper.toDetail(target);
    }

    @Override
    @Transactional
    public AdminUserDetailResponse updateUserProfile(Long userId, AdminUpdateUserProfileRequest request) {
        CustomUserPrincipal principal = requireActiveAdmin();
        validateNotSelfAction(principal.getUserId(), userId);
        if (request == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        // Khóa cả tài khoản và hồ sơ để tuần tự hóa cập nhật quản trị với cập nhật từ người dùng.
        User target = lockManagedUser(userId);
        UserProfile profile = userProfileRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_USER_NOT_FOUND));
        String displayName = profileValidationSupport.normalizeAndValidateDisplayName(request.displayName());
        LocalDate dateOfBirth = profileValidationSupport.validateDateOfBirth(request.dateOfBirth());
        String bio = profileValidationSupport.normalizeAndValidateBio(request.bio());

        profile.setDisplayName(displayName);
        profile.setDateOfBirth(dateOfBirth);
        profile.setBio(bio);

        User adminReference = entityManager.getReference(User.class, principal.getUserId());
        adminActionRepository.save(new AdminAction(
                adminReference,
                AdminActionType.UPDATE_USER_PROFILE,
                AdminTargetType.USER,
                target.getId(),
                "ADMIN_UPDATE_PROFILE"
        ));
        notificationService.createUserProfileUpdatedByAdminNotification(target.getId());

        // Flush trước khi đọc projection để response phản ánh dữ liệu vừa cập nhật trong cùng transaction.
        entityManager.flush();
        AdminUserDetailProjection updated = adminUserRepository.findManagedUserDetail(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_USER_NOT_FOUND));
        return adminUserMapper.toDetail(updated);
    }

    @Override
    public AdminUserDetailResponse updateUserProfileWithAvatar(
            Long userId,
            AdminUpdateUserProfileRequest request,
            AdminAvatarAction avatarAction,
            MultipartFile avatarFile
    ) {
        CustomUserPrincipal principal = requireActiveAdmin();
        validateNotSelfAction(principal.getUserId(), userId);
        validateAvatarRequest(request, avatarAction, avatarFile);
        validateManagedUserTarget(userId);

        CloudinaryUploadResult newAvatar = avatarAction == AdminAvatarAction.REPLACE
                ? cloudinaryStorageService.uploadAvatar(avatarFile)
                : null;
        try {
            AdminProfileUpdateResult result = transactionTemplate.execute(status ->
                    updateProfileWithAvatarInDatabase(principal, userId, request, avatarAction, newAvatar));
            if (result == null) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR);
            }
            deleteOldAvatarAfterDatabaseSuccess(result.oldAvatarPublicId());
            return result.response();
        } catch (RuntimeException exception) {
            cleanupNewAvatarAfterDatabaseFailure(newAvatar);
            throw exception;
        }
    }

    @Override
    @Transactional
    public AdminUserStatusResponse blockUser(Long userId, AdminBlockUserRequest request) {
        CustomUserPrincipal principal = requireActiveAdmin();
        validateNotSelfAction(principal.getUserId(), userId);
        if (request == null || request.reasonCode() == null) {
            throw new BusinessException(ErrorCode.ADMIN_BLOCK_REASON_REQUIRED);
        }

        User target = lockManagedUser(userId);
        if (target.getStatus() == UserStatus.BLOCKED) {
            throw new BusinessException(ErrorCode.ADMIN_USER_ALREADY_BLOCKED);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        String reason = request.reasonCode().name();
        User adminReference = entityManager.getReference(User.class, principal.getUserId());

        // Cập nhật trạng thái trên Entity đang được khóa; JPA dirty checking sẽ ghi trong transaction hiện tại.
        target.setStatus(UserStatus.BLOCKED);
        target.setBlockedAt(now);
        target.setBlockedReason(reason);

        // Bulk update không tải từng Refresh Token. Persistence context phải được giữ để target vẫn là
        // managed entity cho các bản ghi audit và bước refresh response ở cuối transaction.
        refreshTokenRepository.revokeAllActiveByUserId(target.getId(), now);
        accountStatusHistoryRepository.save(new AccountStatusHistory(
                target, UserStatus.ACTIVE, UserStatus.BLOCKED, adminReference, reason));
        adminActionRepository.save(new AdminAction(
                adminReference, AdminActionType.BLOCK_USER, AdminTargetType.USER, target.getId(), reason));
        notificationService.createAccountBlockedNotification(target.getId());

        return flushRefreshAndMap(target);
    }

    @Override
    @Transactional
    public AdminUserStatusResponse unblockUser(Long userId) {
        CustomUserPrincipal principal = requireActiveAdmin();
        validateNotSelfAction(principal.getUserId(), userId);
        User target = lockManagedUser(userId);
        if (target.getStatus() == UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ADMIN_USER_ALREADY_ACTIVE);
        }

        User adminReference = entityManager.getReference(User.class, principal.getUserId());
        target.setStatus(UserStatus.ACTIVE);
        target.setBlockedAt(null);
        target.setBlockedReason(null);

        accountStatusHistoryRepository.save(new AccountStatusHistory(
                target, UserStatus.BLOCKED, UserStatus.ACTIVE, adminReference, UNBLOCK_REASON));
        adminActionRepository.save(new AdminAction(
                adminReference, AdminActionType.UNBLOCK_USER, AdminTargetType.USER,
                target.getId(), UNBLOCK_REASON));
        notificationService.createAccountUnblockedNotification(target.getId());

        // Không gọi repository Refresh Token: token cũ đã revoke phải giữ nguyên sau khi mở khóa.
        return flushRefreshAndMap(target);
    }

    private CustomUserPrincipal requireActiveAdmin() {
        CustomUserPrincipal principal = currentUserProvider.getCurrentUser();
        if (principal.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (principal.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }
        return principal;
    }

    private void validateAvatarRequest(
            AdminUpdateUserProfileRequest request,
            AdminAvatarAction avatarAction,
            MultipartFile avatarFile
    ) {
        if (request == null || avatarAction == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (avatarAction == AdminAvatarAction.REPLACE) {
            avatarFileValidator.validate(avatarFile);
            return;
        }
        if (avatarFile != null && !avatarFile.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void validateManagedUserTarget(Long userId) {
        AdminUserDetailProjection target = adminUserRepository.findManagedUserDetail(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_USER_NOT_FOUND));
        if (!UserRole.USER.name().equals(target.getRole())) {
            throw new BusinessException(ErrorCode.ADMIN_USER_MANAGEMENT_FORBIDDEN);
        }
    }

    private AdminProfileUpdateResult updateProfileWithAvatarInDatabase(
            CustomUserPrincipal principal,
            Long userId,
            AdminUpdateUserProfileRequest request,
            AdminAvatarAction avatarAction,
            CloudinaryUploadResult newAvatar
    ) {
        User target = lockManagedUser(userId);
        UserProfile profile = userProfileRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_USER_NOT_FOUND));

        profile.setDisplayName(profileValidationSupport.normalizeAndValidateDisplayName(request.displayName()));
        profile.setDateOfBirth(profileValidationSupport.validateDateOfBirth(request.dateOfBirth()));
        profile.setBio(profileValidationSupport.normalizeAndValidateBio(request.bio()));

        String oldAvatarPublicId = null;
        if (avatarAction == AdminAvatarAction.REPLACE) {
            oldAvatarPublicId = profile.getAvatarPublicId();
            profile.setAvatarUrl(newAvatar.url());
            profile.setAvatarPublicId(newAvatar.publicId());
        } else if (avatarAction == AdminAvatarAction.REMOVE) {
            oldAvatarPublicId = profile.getAvatarPublicId();
            profile.setAvatarUrl(null);
            profile.setAvatarPublicId(null);
        }

        User adminReference = entityManager.getReference(User.class, principal.getUserId());
        adminActionRepository.save(new AdminAction(
                adminReference,
                AdminActionType.UPDATE_USER_PROFILE,
                AdminTargetType.USER,
                target.getId(),
                avatarAction == AdminAvatarAction.KEEP
                        ? "ADMIN_UPDATE_PROFILE"
                        : "ADMIN_UPDATE_PROFILE_AND_AVATAR"
        ));
        notificationService.createUserProfileUpdatedByAdminNotification(target.getId());

        entityManager.flush();
        AdminUserDetailProjection updated = adminUserRepository.findManagedUserDetail(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_USER_NOT_FOUND));
        return new AdminProfileUpdateResult(adminUserMapper.toDetail(updated), oldAvatarPublicId);
    }

    private void deleteOldAvatarAfterDatabaseSuccess(String oldAvatarPublicId) {
        if (oldAvatarPublicId == null || oldAvatarPublicId.isBlank()) return;
        try {
            cloudinaryStorageService.deleteImage(oldAvatarPublicId);
        } catch (BusinessException exception) {
            // Database đã commit nên lỗi xóa file cũ chỉ được ghi nhận để cleanup sau.
            log.warn("Không thể xóa avatar cũ sau khi ADMIN cập nhật hồ sơ");
        }
    }

    private void cleanupNewAvatarAfterDatabaseFailure(CloudinaryUploadResult newAvatar) {
        if (newAvatar == null || newAvatar.publicId() == null || newAvatar.publicId().isBlank()) return;
        try {
            cloudinaryStorageService.deleteImage(newAvatar.publicId());
        } catch (BusinessException exception) {
            // Không log public_id để tránh lộ metadata lưu trữ.
            log.warn("Không thể cleanup avatar mới sau khi ADMIN cập nhật hồ sơ thất bại");
        }
    }

    private record AdminProfileUpdateResult(
            AdminUserDetailResponse response,
            String oldAvatarPublicId
    ) {
    }

    private void validateNotSelfAction(Long adminId, Long targetId) {
        if (adminId.equals(targetId)) {
            throw new BusinessException(ErrorCode.ADMIN_SELF_ACTION_FORBIDDEN);
        }
    }

    private User lockManagedUser(Long userId) {
        User target = adminUserRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_USER_NOT_FOUND));
        if (target.getRole() != UserRole.USER) {
            throw new BusinessException(ErrorCode.ADMIN_USER_MANAGEMENT_FORBIDDEN);
        }
        return target;
    }

    private AdminUserStatusResponse flushRefreshAndMap(User target) {
        // Flush mọi thay đổi User, token và audit; lỗi tại bất kỳ bảng nào sẽ làm transaction rollback.
        entityManager.flush();
        // updated_at do MySQL quản lý nên refresh để response nhận đúng timestamp vừa cập nhật.
        entityManager.refresh(target);
        return adminUserMapper.toStatus(target);
    }

    private String normalizeAndEscapeOptionalKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        String normalizedKeyword = keyword.trim();
        if (normalizedKeyword.length() > MAX_KEYWORD_LENGTH) {
            throw new BusinessException(ErrorCode.SEARCH_KEYWORD_TOO_LONG);
        }
        return LikePatternEscaper.escape(normalizedKeyword);
    }

    private void validatePagination(int page, int size) {
        // Kiểm tra lại ở Service để use case vẫn an toàn nếu được gọi ngoài HTTP Controller.
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }
}
