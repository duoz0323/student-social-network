package com.stu.edu.vn.backend.admin.rbac.service;

import com.stu.edu.vn.backend.admin.notification.enums.AdminNotificationReferenceType;
import com.stu.edu.vn.backend.admin.notification.enums.AdminNotificationType;
import com.stu.edu.vn.backend.admin.notification.service.AdminNotificationEvent;
import com.stu.edu.vn.backend.admin.notification.service.AdminNotificationRouter;
import com.stu.edu.vn.backend.admin.entity.AdminAction;
import com.stu.edu.vn.backend.admin.collaborator.identity.ManagedSocialIdentityService;
import com.stu.edu.vn.backend.admin.enums.AdminActionType;
import com.stu.edu.vn.backend.admin.enums.AdminTargetType;
import com.stu.edu.vn.backend.admin.rbac.AdminRoleCode;
import com.stu.edu.vn.backend.admin.rbac.dto.AdminAccountListItemResponse;
import com.stu.edu.vn.backend.admin.rbac.dto.AdminAccountResponse;
import com.stu.edu.vn.backend.admin.rbac.dto.AdminRoleResponse;
import com.stu.edu.vn.backend.admin.rbac.dto.AdminPermissionResponse;
import com.stu.edu.vn.backend.admin.rbac.dto.CreateAdminRequest;
import com.stu.edu.vn.backend.admin.rbac.dto.CreateAdminRoleRequest;
import com.stu.edu.vn.backend.admin.rbac.dto.UpdateAdminRequest;
import com.stu.edu.vn.backend.admin.rbac.dto.ResetAdminPasswordRequest;
import com.stu.edu.vn.backend.admin.rbac.dto.ChangeAdminPasswordRequest;
import com.stu.edu.vn.backend.admin.rbac.dto.UpdateAdminProfileRequest;
import com.stu.edu.vn.backend.admin.rbac.entity.AdminRole;
import com.stu.edu.vn.backend.admin.rbac.entity.AdminRoleAssignment;
import com.stu.edu.vn.backend.admin.rbac.entity.AdminRoleAssignmentId;
import com.stu.edu.vn.backend.admin.rbac.repository.AdminAccountListProjection;
import com.stu.edu.vn.backend.admin.rbac.repository.AdminAccountRepository;
import com.stu.edu.vn.backend.admin.rbac.repository.AdminPermissionRepository;
import com.stu.edu.vn.backend.admin.rbac.repository.AdminRoleAssignmentRepository;
import com.stu.edu.vn.backend.admin.rbac.repository.AdminRoleRepository;
import com.stu.edu.vn.backend.admin.repository.AdminActionRepository;
import com.stu.edu.vn.backend.auth.repository.RefreshTokenRepository;
import com.stu.edu.vn.backend.auth.support.EmailNormalizer;
import com.stu.edu.vn.backend.auth.support.PasswordPolicyValidator;
import com.stu.edu.vn.backend.common.api.PageResponse;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.security.AdminAuthorization;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import com.stu.edu.vn.backend.user.service.impl.UserProfileValidationSupport;
import java.time.Clock;
import java.time.LocalDateTime;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

/** Triển khai vòng đời tài khoản admin, gán role và bảo vệ Master Admin bất biến. */
@Service
@RequiredArgsConstructor
public class AdminManagementServiceImpl implements AdminManagementService {

    private static final Set<String> NON_DELEGABLE_PERMISSION_CODES = Set.of(
            "ADMIN_CREATE", "ADMIN_ROLE_ASSIGN", "ADMIN_ROLE_REVOKE");

    private final AdminAccountRepository adminAccountRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final AdminRoleRepository roleRepository;
    private final AdminRoleAssignmentRepository assignmentRepository;
    private final AdminPermissionRepository permissionRepository;
    private final DatabaseAdminAuthorityResolver authorityResolver;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AdminActionRepository actionRepository;
    private final CurrentUserProvider currentUserProvider;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final UserProfileValidationSupport profileValidationSupport;
    private final PasswordEncoder passwordEncoder;
    private final ManagedSocialIdentityService managedSocialIdentityService;
    private final Clock clock;
    private AdminNotificationRouter adminNotificationRouter;

    @Autowired
    void setAdminNotificationRouter(AdminNotificationRouter adminNotificationRouter) {
        this.adminNotificationRouter = adminNotificationRouter;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminAccountListItemResponse> getAdmins(
            String keyword, String status, int page, int size
    ) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        String normalizedStatus = normalizeStatus(status);
        Page<AdminAccountListItemResponse> result = adminAccountRepository
                .findAdmins(normalizedKeyword, normalizedStatus, PageRequest.of(page, size))
                .map(this::toListItem);
        return PageResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminAccountResponse getAdmin(Long adminId) {
        return toResponse(requireAdmin(adminId));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminAccountResponse getCurrentAdminProfile() {
        return toResponse(requireAdmin(currentUserProvider.getCurrentUserId()));
    }

    @Override
    @Transactional
    public AdminAccountResponse updateCurrentAdminProfile(UpdateAdminProfileRequest request) {
        Long adminId = currentUserProvider.getCurrentUserId();
        User admin = requireAdminForUpdate(adminId);
        UserProfile profile = profileRepository.findByIdForUpdate(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_ACCOUNT_NOT_FOUND));
        profile.setDisplayName(profileValidationSupport.normalizeAndValidateDisplayName(request.displayName()));
        profile.setDateOfBirth(profileValidationSupport.validateDateOfBirth(request.dateOfBirth()));
        profile.setBio(profileValidationSupport.normalizeAndValidateBio(request.bio()));
        profileRepository.save(profile);
        saveAudit(adminId, AdminActionType.UPDATE_ADMIN_PROFILE, adminId,
                "Quản trị viên tự cập nhật tên hiển thị, ngày sinh hoặc giới thiệu");
        return toResponse(admin);
    }

    @Override
    @Transactional
    public void changeCurrentAdminPassword(ChangeAdminPasswordRequest request) {
        Long adminId = currentUserProvider.getCurrentUserId();
        User admin = requireAdminForUpdate(adminId);
        if (admin.getPasswordHash() == null
                || !passwordEncoder.matches(request.currentPassword(), admin.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_CURRENT_PASSWORD_INVALID);
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRMATION_NOT_MATCH);
        }
        if (!passwordPolicyValidator.isValid(request.newPassword())) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_POLICY_VIOLATION);
        }
        if (passwordEncoder.matches(request.newPassword(), admin.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_MUST_BE_DIFFERENT);
        }

        // Thu hồi toàn bộ phiên để mật khẩu bị lộ không thể tiếp tục tạo Access Token mới.
        admin.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        revokeSessions(adminId);
        saveAudit(adminId, AdminActionType.CHANGE_ADMIN_PASSWORD, adminId,
                "Quản trị viên tự đổi mật khẩu; không lưu mật khẩu trong lịch sử");
    }

    @Override
    @Transactional
    public AdminAccountResponse createAdmin(CreateAdminRequest request) {
        String email = EmailNormalizer.normalize(request.email()).value();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRMATION_NOT_MATCH);
        }
        if (!passwordPolicyValidator.isValid(request.password())) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_POLICY_VIOLATION);
        }
        String username = profileValidationSupport.normalizeAndValidateUsername(request.username());
        if (profileRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        String displayName = profileValidationSupport.normalizeAndValidateDisplayName(request.displayName());
        var dateOfBirth = profileValidationSupport.validateDateOfBirth(request.dateOfBirth());
        List<AdminRole> roles = requireRoles(request.roleCodes());
        rejectSuperAdminRole(roles);
        LocalDateTime now = LocalDateTime.now(clock);

        User admin = new User(email, passwordEncoder.encode(request.password()));
        admin.setEmailVerifiedAt(now);
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        User savedAdmin = userRepository.saveAndFlush(admin);

        UserProfile profile = new UserProfile(savedAdmin);
        profile.setUsername(username);
        profile.setDisplayName(displayName);
        profile.setDateOfBirth(dateOfBirth);
        profile.setProfileCompletedAt(now);
        profileRepository.saveAndFlush(profile);

        Long actorId = currentUserProvider.getCurrentUserId();
        roles.forEach(role -> assignmentRepository.save(
                new AdminRoleAssignment(savedAdmin.getId(), role.getId(), actorId)));
        if (roles.stream().anyMatch(role -> AdminRoleCode.COLLABORATOR.name().equals(role.getCode()))) {
            managedSocialIdentityService.activateOrCreateForRole(savedAdmin, requireActor(actorId));
        }
        AdminAction action = saveAudit(actorId, AdminActionType.CREATE_ADMIN, savedAdmin.getId(),
                "roles=" + roles.stream().map(AdminRole::getCode).sorted().toList());
        notifyAdminOversight(actorId, savedAdmin.getId(), action == null ? null : action.getId(), AdminNotificationType.ADMIN_CREATED,
                "Tài khoản quản trị mới đã được tạo");
        return toResponse(savedAdmin);
    }

    @Override
    @Transactional
    public AdminAccountResponse updateAdmin(Long adminId, UpdateAdminRequest request) {
        User admin = requireAdminForUpdate(adminId);
        UserProfile profile = profileRepository.findByIdForUpdate(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_ACCOUNT_NOT_FOUND));
        profile.setDisplayName(profileValidationSupport.normalizeAndValidateDisplayName(request.displayName()));
        profileRepository.save(profile);
        Long actorId = currentUserProvider.getCurrentUserId();
        AdminAction action = saveAudit(actorId, AdminActionType.UPDATE_ADMIN, adminId,
                "Cập nhật tên hiển thị admin");
        notifyAdminOversight(actorId, adminId, action == null ? null : action.getId(), AdminNotificationType.ADMIN_UPDATED,
                "Tài khoản quản trị đã được cập nhật");
        return toResponse(admin);
    }

    @Override
    @Transactional
    public AdminAccountResponse disableAdmin(Long adminId) {
        User admin = requireAdminForUpdate(adminId);
        if (admin.getStatus() == UserStatus.BLOCKED) {
            throw new BusinessException(ErrorCode.ADMIN_ACCOUNT_ALREADY_DISABLED);
        }
        protectMasterAdmin(adminId);
        LocalDateTime now = LocalDateTime.now(clock);
        admin.setStatus(UserStatus.BLOCKED);
        admin.setBlockedAt(now);
        admin.setBlockedReason("ADMIN_DISABLED");
        refreshTokenRepository.revokeAllActiveByUserId(adminId, now);
        Long actorId = currentUserProvider.getCurrentUserId();
        AdminAction action = saveAudit(actorId, AdminActionType.DISABLE_ADMIN, adminId,
                "Vô hiệu hóa tài khoản quản trị");
        notifyAdminOversight(actorId, adminId, action == null ? null : action.getId(), AdminNotificationType.ADMIN_DISABLED,
                "Tài khoản quản trị đã bị vô hiệu hóa");
        return toResponse(admin);
    }

    @Override
    @Transactional
    public AdminAccountResponse enableAdmin(Long adminId) {
        User admin = requireAdminForUpdate(adminId);
        if (admin.getStatus() == UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ADMIN_ACCOUNT_ALREADY_ENABLED);
        }
        admin.setStatus(UserStatus.ACTIVE);
        admin.setBlockedAt(null);
        admin.setBlockedReason(null);
        Long actorId = currentUserProvider.getCurrentUserId();
        AdminAction action = saveAudit(actorId, AdminActionType.ENABLE_ADMIN, adminId,
                "Mở khóa tài khoản quản trị");
        notifyAdminOversight(actorId, adminId, action == null ? null : action.getId(), AdminNotificationType.ADMIN_ENABLED,
                "Tài khoản quản trị đã được kích hoạt");
        return toResponse(admin);
    }

    @Override
    @Transactional
    public void resetPassword(Long adminId, ResetAdminPasswordRequest request) {
        User admin = requireAdminForUpdate(adminId);
        protectMasterAdmin(adminId);
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRMATION_NOT_MATCH);
        }
        if (!passwordPolicyValidator.isValid(request.newPassword())) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_POLICY_VIOLATION);
        }
        if (admin.getPasswordHash() != null && passwordEncoder.matches(request.newPassword(), admin.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_MUST_BE_DIFFERENT);
        }

        // Cấp lại mật khẩu và thu hồi phiên trong cùng transaction để mật khẩu cũ không còn tạo phiên mới.
        admin.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        revokeSessions(adminId);
        Long actorId = currentUserProvider.getCurrentUserId();
        AdminAction action = saveAudit(actorId, AdminActionType.RESET_ADMIN_PASSWORD, adminId,
                "Cấp lại mật khẩu quản trị; không lưu giá trị mật khẩu trong lịch sử");
        notifyAdminOversight(actorId, adminId, action == null ? null : action.getId(), AdminNotificationType.ADMIN_PASSWORD_RESET,
                "Mật khẩu tài khoản quản trị đã được cấp lại");
    }

    @Override
    @Transactional
    public AdminAccountResponse assignRole(Long adminId, String roleCode) {
        User admin = requireAdminForUpdate(adminId);
        protectMasterAdmin(adminId);
        AdminRole role = requireRole(roleCode, false);
        if (AdminRoleCode.SUPER_ADMIN.name().equals(role.getCode())) {
            throw new BusinessException(ErrorCode.ADMIN_SUPER_ROLE_BOOTSTRAP_ONLY);
        }
        AdminRoleAssignmentId id = new AdminRoleAssignmentId(adminId, role.getId());
        if (assignmentRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.ADMIN_ROLE_ALREADY_ASSIGNED);
        }
        Long actorId = currentUserProvider.getCurrentUserId();
        assignmentRepository.save(new AdminRoleAssignment(adminId, role.getId(), actorId));
        if (AdminRoleCode.COLLABORATOR.name().equals(role.getCode())) {
            managedSocialIdentityService.activateOrCreateForRole(admin, requireActor(actorId));
        }
        revokeSessions(adminId);
        AdminAction action = saveAudit(actorId, AdminActionType.ASSIGN_ADMIN_ROLE, adminId, "role=" + role.getCode());
        notifyRoleChange(actorId, adminId, role, action == null ? null : action.getId(), true);
        return toResponse(admin);
    }

    @Override
    @Transactional
    public AdminAccountResponse revokeRole(Long adminId, String roleCode) {
        User admin = requireAdminForUpdate(adminId);
        protectMasterAdmin(adminId);
        AdminRole role = requireRole(roleCode, AdminRoleCode.SUPER_ADMIN.name().equals(normalizeRoleCode(roleCode)));
        AdminRoleAssignment assignment = assignmentRepository
                .findByIdForUpdate(new AdminRoleAssignmentId(adminId, role.getId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_ROLE_NOT_ASSIGNED));
        if (assignmentRepository.countByIdAdminId(adminId) <= 1) {
            throw new BusinessException(ErrorCode.ADMIN_ROLE_REQUIRED);
        }
        assignmentRepository.delete(assignment);
        if (AdminRoleCode.COLLABORATOR.name().equals(role.getCode())) {
            managedSocialIdentityService.disableForRoleRevocation(adminId,
                    requireActor(currentUserProvider.getCurrentUserId()));
        }
        revokeSessions(adminId);
        Long actorId = currentUserProvider.getCurrentUserId();
        AdminAction action = saveAudit(actorId, AdminActionType.REVOKE_ADMIN_ROLE, adminId,
                "role=" + role.getCode());
        notifyRoleChange(actorId, adminId, role, action == null ? null : action.getId(), false);
        return toResponse(admin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminRoleResponse> getRoleCatalog() {
        return roleRepository.findAllByOrderByIdAsc().stream()
                .map(role -> new AdminRoleResponse(
                        role.getCode(), role.getDisplayName(), role.getDescription(), role.isReserved(),
                        new LinkedHashSet<>(permissionRepository.findCodesByRoleId(role.getId()))))
                .toList();
    }

    @Override
    @Transactional
    public AdminRoleResponse createRole(CreateAdminRoleRequest request) {
        String displayName = normalizeRoleDisplayName(request.name());
        String code = generateRoleCode(displayName);
        if (roleRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.ADMIN_ROLE_ALREADY_EXISTS);
        }

        AdminRole role;
        try {
            role = roleRepository.saveAndFlush(new AdminRole(
                    code, displayName, "Vai trò tùy chỉnh được tạo từ giao diện quản trị."));
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.ADMIN_ROLE_ALREADY_EXISTS);
        }

        Set<String> defaultPermissions = Set.of("DASHBOARD_BASIC_VIEW");
        if (permissionRepository.findAllByCodeIn(defaultPermissions).size() != 1) {
            throw new BusinessException(ErrorCode.ADMIN_PERMISSION_NOT_FOUND);
        }
        permissionRepository.insertMappings(role.getId(), defaultPermissions);
        Long actorId = currentUserProvider.getCurrentUserId();
        AdminAction action = saveAudit(actorId, AdminActionType.CREATE_ADMIN_ROLE, actorId,
                "roleId=" + role.getId() + "; code=" + code + "; name=" + displayName);
        notifyRoleOversight(actorId, role, action == null ? null : action.getId(), AdminNotificationType.ADMIN_ROLE_CREATED,
                "Vai trò quản trị mới đã được tạo", false);
        return new AdminRoleResponse(
                role.getCode(), role.getDisplayName(), role.getDescription(), role.isReserved(), defaultPermissions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminPermissionResponse> getPermissionCatalog() {
        return permissionRepository.findAllByOrderByCodeAsc().stream()
                .map(permission -> new AdminPermissionResponse(
                        permission.getCode(), permission.getDescription(), permissionModule(permission.getCode())))
                .toList();
    }

    @Override
    @Transactional
    public AdminRoleResponse updateRolePermissions(String roleCode, Set<String> permissionCodes) {
        AdminRole role = requireRole(roleCode, true);
        if (AdminRoleCode.SUPER_ADMIN.name().equals(role.getCode())) {
            throw new BusinessException(ErrorCode.ADMIN_SUPER_PERMISSIONS_IMMUTABLE);
        }
        if (AdminRoleCode.COLLABORATOR.name().equals(role.getCode())) {
            throw new BusinessException(ErrorCode.ADMIN_COLLABORATOR_PERMISSIONS_IMMUTABLE);
        }

        Set<String> normalizedCodes = permissionCodes == null
                ? Set.of()
                : permissionCodes.stream()
                        .filter(java.util.Objects::nonNull)
                        .map(code -> code.trim().toUpperCase(Locale.ROOT))
                        .filter(code -> !code.isBlank())
                        .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!normalizedCodes.contains("DASHBOARD_BASIC_VIEW")) {
            throw new BusinessException(ErrorCode.ADMIN_DASHBOARD_PERMISSION_REQUIRED);
        }
        if (normalizedCodes.stream().anyMatch(NON_DELEGABLE_PERMISSION_CODES::contains)) {
            throw new BusinessException(ErrorCode.ADMIN_PERMISSION_NOT_DELEGABLE);
        }
        if (permissionRepository.findAllByCodeIn(normalizedCodes).size() != normalizedCodes.size()) {
            throw new BusinessException(ErrorCode.ADMIN_PERMISSION_NOT_FOUND);
        }

        Set<String> oldCodes = new LinkedHashSet<>(permissionRepository.findCodesByRoleId(role.getId()));
        permissionRepository.deleteMappingsByRoleId(role.getId());
        permissionRepository.insertMappings(role.getId(), normalizedCodes);

        LocalDateTime now = LocalDateTime.now(clock);
        assignmentRepository.findAdminIdsByRoleId(role.getId())
                .forEach(adminId -> refreshTokenRepository.revokeAllActiveByUserId(adminId, now));
        Long actorId = currentUserProvider.getCurrentUserId();
        AdminAction action = saveAudit(actorId, AdminActionType.UPDATE_ROLE_PERMISSIONS, actorId,
                "role=" + role.getCode() + "; old=" + oldCodes + "; new=" + normalizedCodes);
        notifyRoleOversight(actorId, role, action == null ? null : action.getId(), AdminNotificationType.ADMIN_ROLE_PERMISSIONS_UPDATED,
                "Quyền của vai trò " + role.getDisplayName() + " đã được cập nhật", true);
        return new AdminRoleResponse(
                role.getCode(), role.getDisplayName(), role.getDescription(), role.isReserved(), normalizedCodes);
    }

    private String permissionModule(String code) {
        if (code.startsWith("USER_")) return "USER_MANAGEMENT";
        if (code.startsWith("POST_")) return "POST_MANAGEMENT";
        if (code.startsWith("HASHTAG_")) return "HASHTAG_MANAGEMENT";
        if (code.startsWith("REPORT_") || code.startsWith("MODERATION_")) return "REPORT_MANAGEMENT";
        if (code.startsWith("ADMIN_")) return "ADMIN_MANAGEMENT";
        return "DASHBOARD";
    }

    private User requireAdmin(Long adminId) {
        User user = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_ACCOUNT_NOT_FOUND));
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.ADMIN_ACCOUNT_NOT_FOUND);
        }
        return user;
    }

    private User requireAdminForUpdate(Long adminId) {
        User user = userRepository.findByIdForUpdate(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_ACCOUNT_NOT_FOUND));
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.ADMIN_ACCOUNT_NOT_FOUND);
        }
        return user;
    }

    private List<AdminRole> requireRoles(Set<String> requestedCodes) {
        if (requestedCodes == null || requestedCodes.isEmpty()) {
            throw new BusinessException(ErrorCode.ADMIN_ROLE_REQUIRED);
        }
        Set<String> codes = requestedCodes.stream().map(this::normalizeRoleCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<AdminRole> roles = roleRepository.findAllByCodeIn(codes);
        if (roles.size() != codes.size()) {
            throw new BusinessException(ErrorCode.ADMIN_ROLE_NOT_FOUND);
        }
        return roles;
    }

    private void rejectSuperAdminRole(List<AdminRole> roles) {
        if (roles.stream().anyMatch(role -> AdminRoleCode.SUPER_ADMIN.name().equals(role.getCode()))) {
            throw new BusinessException(ErrorCode.ADMIN_SUPER_ROLE_BOOTSTRAP_ONLY);
        }
    }

    private AdminRole requireRole(String rawCode, boolean lock) {
        String code = normalizeRoleCode(rawCode);
        return (lock ? roleRepository.findByCodeForUpdate(code) : roleRepository.findByCode(code))
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_ROLE_NOT_FOUND));
    }

    private String normalizeRoleCode(String roleCode) {
        String code = roleCode == null ? "" : roleCode.trim().toUpperCase(Locale.ROOT);
        if (!code.matches("[A-Z][A-Z0-9_]{1,63}")) {
            throw new BusinessException(ErrorCode.ADMIN_ROLE_NOT_FOUND);
        }
        return code;
    }

    private String normalizeRoleDisplayName(String rawName) {
        String name = rawName == null ? "" : rawName.trim().replaceAll("(?U)\\s+", " ");
        if (name.length() < 2 || name.length() > 100) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        return name;
    }

    private String generateRoleCode(String displayName) {
        String ascii = Normalizer.normalize(displayName.replace('đ', 'd').replace('Đ', 'D'), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (ascii.isBlank()) {
            throw new BusinessException(ErrorCode.ADMIN_ROLE_NAME_INVALID);
        }
        if (Character.isDigit(ascii.charAt(0))) {
            ascii = "ROLE_" + ascii;
        }
        if (ascii.length() > 64) {
            ascii = ascii.substring(0, 64).replaceAll("_+$", "");
        }
        return ascii;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return UserStatus.valueOf(status.trim().toUpperCase(Locale.ROOT)).name();
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void protectMasterAdmin(Long adminId) {
        // Theo contract, chỉ tài khoản Bootstrap/Master Admin được giữ SUPER_ADMIN.
        if (assignmentRepository.findRoleCodes(adminId).contains(AdminRoleCode.SUPER_ADMIN.name())) {
            throw new BusinessException(ErrorCode.ADMIN_MASTER_ACCOUNT_PROTECTED);
        }
    }

    private void revokeSessions(Long adminId) {
        refreshTokenRepository.revokeAllActiveByUserId(adminId, LocalDateTime.now(clock));
    }

    private AdminAction saveAudit(Long actorId, AdminActionType actionType, Long targetId, String note) {
        User actor = requireActor(actorId);
        return actionRepository.save(new AdminAction(actor, actionType, AdminTargetType.USER, targetId, note));
    }

    private void notifyAdminOversight(Long actorId, Long adminId, Long actionId,
            AdminNotificationType type, String title) {
        if (adminNotificationRouter == null) return;
        adminNotificationRouter.notifyByPermission("ADMIN_VIEW", actorId, new AdminNotificationEvent(
                type, title, title + ".", com.stu.edu.vn.backend.admin.notification.enums.AdminNotificationReferenceType.ADMIN,
                adminId, "ADMIN_ACTION:" + actionId + ":" + type.name()));
    }

    private void notifyRoleChange(Long actorId, Long targetAdminId, AdminRole role, Long actionId, boolean assigned) {
        if (adminNotificationRouter == null) return;
        AdminNotificationType type = assigned ? AdminNotificationType.ADMIN_ROLE_ASSIGNED
                : AdminNotificationType.ADMIN_ROLE_REVOKED;
        String title = assigned ? "Bạn đã được gán vai trò " + role.getDisplayName()
                : "Vai trò " + role.getDisplayName() + " đã bị thu hồi";
        AdminNotificationEvent event = new AdminNotificationEvent(type, title, title + ".",
                AdminNotificationReferenceType.ADMIN, targetAdminId,
                "ADMIN_ACTION:" + actionId + ":" + type.name());
        // Direct trước để holder không nhận thêm một row oversight cho cùng event key.
        adminNotificationRouter.notifyDirectAdmin(targetAdminId, actorId, event);
        adminNotificationRouter.notifyByPermission("ADMIN_VIEW", actorId, event);
    }

    private void notifyRoleOversight(Long actorId, AdminRole role, Long actionId,
            AdminNotificationType type, String title, boolean notifyHolders) {
        if (adminNotificationRouter == null) return;
        AdminNotificationEvent event = new AdminNotificationEvent(type, title, title + ".",
                AdminNotificationReferenceType.ROLE, role.getId(),
                "ADMIN_ACTION:" + actionId + ":" + type.name());
        if (notifyHolders) adminNotificationRouter.notifyRoleHolders(role.getId(), actorId, event);
        adminNotificationRouter.notifyByPermission("ADMIN_VIEW", actorId, event);
    }

    private User requireActor(Long actorId) {
        return userRepository.findById(actorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }

    private AdminAccountListItemResponse toListItem(AdminAccountListProjection row) {
        Set<String> roles = row.getRoleCodes() == null || row.getRoleCodes().isBlank()
                ? Set.of()
                : new LinkedHashSet<>(Arrays.asList(row.getRoleCodes().split(",")));
        return new AdminAccountListItemResponse(row.getAdminId(), row.getEmail(), row.getUsername(),
                row.getDisplayName(), row.getStatus(), roles, row.getCreatedAt());
    }

    private AdminAccountResponse toResponse(User admin) {
        UserProfile profile = profileRepository.findById(admin.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_ACCOUNT_NOT_FOUND));
        AdminAuthorization authorization = authorityResolver.resolve(admin.getId());
        return new AdminAccountResponse(
                admin.getId(), admin.getEmail(), profile.getUsername(), profile.getDisplayName(),
                profile.getAvatarUrl(), profile.getBio(), profile.getDateOfBirth(), admin.getStatus().name(), authorization.roles(),
                authorization.permissions(), admin.getCreatedAt(), admin.getUpdatedAt());
    }
}
