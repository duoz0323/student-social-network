package com.stu.edu.vn.backend.admin.collaborator.identity;

import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.rbac.AdminRoleCode;
import com.stu.edu.vn.backend.admin.rbac.repository.AdminRoleAssignmentRepository;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserAccountType;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.service.impl.UserProfileValidationSupport;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ManagedSocialIdentityService {
    private final AdminSocialIdentityRepository identityRepository;
    private final CollaboratorSocialIdentityResolver identityResolver;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final AdminRoleAssignmentRepository roleAssignmentRepository;
    private final UserProfileValidationSupport profileValidation;
    private final CurrentUserProvider currentUserProvider;
    private final AdminActionRepository actionRepository;
    private final Clock clock;

    @Transactional
    public ManagedSocialIdentityResponse getCurrent() {
        Long adminId = currentUserProvider.getCurrentUserId();
        if (!identityRepository.existsByAdminId(adminId)) {
            // Tự đồng bộ dữ liệu legacy đã có role trước migration; role DB vẫn là điều kiện bắt buộc.
            User admin = userRepository.findByIdForUpdate(adminId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_ACCOUNT_NOT_FOUND));
            if (admin.getRole() != UserRole.ADMIN
                    || !roleAssignmentRepository.findRoleCodes(adminId)
                            .contains(AdminRoleCode.COLLABORATOR.name())) {
                throw new BusinessException(ErrorCode.ADMIN_ROLE_NOT_ASSIGNED);
            }
            activateOrCreateForRole(admin, admin);
        }
        User socialUser = identityResolver.resolveActive(adminId);
        return response(socialUser);
    }

    @Transactional
    public ManagedSocialIdentityResponse create(Long adminId, CreateManagedSocialIdentityRequest request) {
        if (identityRepository.existsByAdminId(adminId)) {
            throw new BusinessException(ErrorCode.COLLABORATOR_SOCIAL_IDENTITY_ALREADY_EXISTS);
        }
        User admin = userRepository.findByIdForUpdate(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_ACCOUNT_NOT_FOUND));
        if (admin.getRole() != UserRole.ADMIN
                || !roleAssignmentRepository.findRoleCodes(adminId).contains(AdminRoleCode.COLLABORATOR.name())) {
            throw new BusinessException(ErrorCode.ADMIN_ROLE_NOT_ASSIGNED);
        }
        String username = profileValidation.normalizeAndValidateUsername(request.username());
        if (profileRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        String displayName = profileValidation.normalizeAndValidateDisplayName(request.displayName());
        String bio = profileValidation.normalizeAndValidateBio(request.bio());
        User actor = userRepository.findById(currentUserProvider.getCurrentUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        return response(createIdentity(admin, actor, username, displayName,
                normalizeNullable(request.avatarUrl()), bio));
    }

    /** Gán role COLLABORATOR phải đồng thời bảo đảm tài khoản dùng được ngay khu vực cộng tác viên. */
    @Transactional
    public void activateOrCreateForRole(User admin, User actor) {
        AdminSocialIdentity existing = identityRepository.findByAdminId(admin.getId()).orElse(null);
        if (existing != null) {
            if (existing.getStatus() == ManagedSocialIdentityStatus.DISABLED) {
                existing.setStatus(ManagedSocialIdentityStatus.ACTIVE);
                actionRepository.save(new AdminAction(actor, AdminActionType.CREATE_MANAGED_SOCIAL_IDENTITY,
                        AdminTargetType.USER, existing.getSocialUser().getId(),
                        "reactivated=true; collaboratorAdminId=" + admin.getId()));
            }
            return;
        }

        String username = nextDefaultUsername(admin.getId());
        String displayName = profileValidation.normalizeAndValidateDisplayName("Kênh UniShare");
        createIdentity(admin, actor, username, displayName, null,
                profileValidation.normalizeAndValidateBio("Tài khoản nội dung được quản lý bởi UniShare."));
    }

    /** Thu hồi role chỉ vô hiệu hóa quyền điều khiển, không xóa Social User, bài viết hoặc lịch sử. */
    @Transactional
    public void disableForRoleRevocation(Long adminId, User actor) {
        identityRepository.findByAdminId(adminId).ifPresent(identity -> {
            if (identity.getStatus() == ManagedSocialIdentityStatus.ACTIVE) {
                identity.setStatus(ManagedSocialIdentityStatus.DISABLED);
                actionRepository.save(new AdminAction(actor, AdminActionType.DISABLE_MANAGED_SOCIAL_IDENTITY,
                        AdminTargetType.USER, identity.getSocialUser().getId(),
                        "collaboratorAdminId=" + adminId));
            }
        });
    }

    private User createIdentity(User admin, User actor, String username, String displayName,
                                String avatarUrl, String bio) {
        User managedUser = new User(null, null);
        managedUser.setRole(UserRole.USER);
        managedUser.setStatus(UserStatus.ACTIVE);
        managedUser.setAccountType(UserAccountType.MANAGED);
        User savedUser = userRepository.saveAndFlush(managedUser);

        UserProfile profile = new UserProfile(savedUser);
        profile.setUsername(username);
        profile.setDisplayName(displayName);
        profile.setAvatarUrl(avatarUrl);
        profile.setBio(bio);
        profile.setProfileCompletedAt(LocalDateTime.now(clock));
        profileRepository.saveAndFlush(profile);

        identityRepository.saveAndFlush(new AdminSocialIdentity(admin, savedUser, actor));
        actionRepository.save(new AdminAction(actor, AdminActionType.CREATE_MANAGED_SOCIAL_IDENTITY,
                AdminTargetType.USER, savedUser.getId(), "collaboratorAdminId=" + admin.getId()));
        return savedUser;
    }

    private String nextDefaultUsername(Long adminId) {
        String base = "collab_" + Long.toUnsignedString(adminId, 36);
        for (int suffix = 0; suffix < 100; suffix++) {
            String candidate = suffix == 0 ? base : base + "_" + suffix;
            String username = profileValidation.normalizeAndValidateUsername(candidate);
            if (!profileRepository.existsByUsername(username)) return username;
        }
        throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
    }

    private ManagedSocialIdentityResponse response(User socialUser) {
        UserProfile profile = profileRepository.findById(socialUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        return new ManagedSocialIdentityResponse(socialUser.getId(), profile.getUsername(), profile.getDisplayName(),
                profile.getAvatarUrl(), profile.getBio(), true);
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
