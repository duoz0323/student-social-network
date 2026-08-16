package com.stu.edu.vn.backend.admin.bootstrap;

import com.stu.edu.vn.backend.auth.support.EmailNormalizer;
import com.stu.edu.vn.backend.admin.rbac.AdminRoleCode;
import com.stu.edu.vn.backend.admin.rbac.entity.AdminRole;
import com.stu.edu.vn.backend.admin.rbac.entity.AdminRoleAssignment;
import com.stu.edu.vn.backend.admin.rbac.entity.AdminRoleAssignmentId;
import com.stu.edu.vn.backend.admin.rbac.repository.AdminRoleAssignmentRepository;
import com.stu.edu.vn.backend.admin.rbac.repository.AdminRoleRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tạo tài khoản ADMIN khởi đầu và hồ sơ hoàn tất trong cùng một transaction.
 */
@Service
public class BootstrapAdminService {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 72;
    private static final int MIN_DISPLAY_NAME_LENGTH = 2;
    private static final int MAX_DISPLAY_NAME_LENGTH = 100;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9._]{3,30}$");
    private static final int MINIMUM_AGE = 18;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$"
    );

    private final BootstrapAdminProperties properties;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final AdminRoleRepository adminRoleRepository;
    private final AdminRoleAssignmentRepository adminRoleAssignmentRepository;

    public BootstrapAdminService(
            BootstrapAdminProperties properties,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            PasswordEncoder passwordEncoder,
            Clock clock
    ) {
        this(properties, userRepository, userProfileRepository, passwordEncoder, clock, null, null);
    }

    @Autowired
    public BootstrapAdminService(
            BootstrapAdminProperties properties,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            PasswordEncoder passwordEncoder,
            Clock clock,
            AdminRoleRepository adminRoleRepository,
            AdminRoleAssignmentRepository adminRoleAssignmentRepository
    ) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.adminRoleRepository = adminRoleRepository;
        this.adminRoleAssignmentRepository = adminRoleAssignmentRepository;
    }

    @Transactional
    public BootstrapAdminResult bootstrapIfEnabled() {
        if (!properties.isEnabled()) {
            return BootstrapAdminResult.DISABLED;
        }

        String normalizedEmail = normalizeAndValidateEmail(properties.getEmail());
        String password = validatePassword(properties.getPassword());
        String username = normalizeAndValidateUsername(properties.getUsername());
        String displayName = normalizeAndValidateDisplayName(properties.getDisplayName());
        LocalDate dateOfBirth = validateDateOfBirth(properties.getDateOfBirth());

        // Email tồn tại chỉ làm bootstrap bỏ qua, tuyệt đối không nâng role hoặc đổi mật khẩu tài khoản cũ.
        if (userRepository.existsByEmail(normalizedEmail)) {
            // Chỉ lookup thêm khi RBAC repository hiện diện; giữ tính tương thích cho unit test constructor rút gọn.
            if (adminRoleRepository != null) {
                userRepository.findByEmail(normalizedEmail)
                        .filter(user -> user.getRole() == UserRole.ADMIN)
                        .ifPresent(user -> ensureSuperAdminRole(user.getId()));
            }
            return BootstrapAdminResult.ALREADY_EXISTS;
        }

        User admin = new User(normalizedEmail, passwordEncoder.encode(password));
        admin.setEmailVerifiedAt(LocalDateTime.now(clock));
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setBlockedAt(null);
        admin.setBlockedReason(null);
        User savedAdmin = userRepository.saveAndFlush(admin);

        UserProfile profile = new UserProfile(savedAdmin);
        profile.setUsername(username);
        profile.setDisplayName(displayName);
        profile.setAvatarUrl(null);
        profile.setAvatarPublicId(null);
        profile.setBio(null);
        profile.setDateOfBirth(dateOfBirth);
        // Tài khoản quản trị khởi tạo cần vượt qua profile guard để sử dụng API quản trị ngay sau khi tạo.
        profile.setProfileCompletedAt(LocalDateTime.now(clock));
        // saveAndFlush làm lỗi hồ sơ xuất hiện ngay trong transaction để bản ghi users được rollback cùng lúc.
        userProfileRepository.saveAndFlush(profile);
        ensureSuperAdminRole(savedAdmin.getId());

        return BootstrapAdminResult.CREATED;
    }

    private void ensureSuperAdminRole(Long adminId) {
        // Unit test cũ dùng constructor rút gọn; production luôn được Spring truyền repository RBAC.
        if (adminRoleRepository == null || adminRoleAssignmentRepository == null) {
            return;
        }
        AdminRole role = adminRoleRepository.findByCode(AdminRoleCode.SUPER_ADMIN.name())
                .orElseThrow(() -> new IllegalStateException("RBAC role SUPER_ADMIN chưa được seed"));
        AdminRoleAssignmentId id = new AdminRoleAssignmentId(adminId, role.getId());
        if (!adminRoleAssignmentRepository.existsById(id)) {
            adminRoleAssignmentRepository.save(new AdminRoleAssignment(adminId, role.getId(), null));
        }
    }

    private String normalizeAndValidateEmail(String rawEmail) {
        try {
            return EmailNormalizer.normalize(rawEmail).value();
        } catch (BusinessException exception) {
            throw invalidConfiguration("BOOTSTRAP_ADMIN_EMAIL");
        }
    }

    private String validatePassword(String rawPassword) {
        if (rawPassword == null
                || rawPassword.length() < MIN_PASSWORD_LENGTH
                || rawPassword.length() > MAX_PASSWORD_LENGTH
                || !PASSWORD_PATTERN.matcher(rawPassword).matches()) {
            throw invalidConfiguration("BOOTSTRAP_ADMIN_PASSWORD");
        }
        return rawPassword;
    }

    private String normalizeAndValidateDisplayName(String rawDisplayName) {
        String displayName = rawDisplayName == null ? "" : rawDisplayName.trim();
        if (displayName.length() < MIN_DISPLAY_NAME_LENGTH || displayName.length() > MAX_DISPLAY_NAME_LENGTH) {
            throw invalidConfiguration("BOOTSTRAP_ADMIN_DISPLAY_NAME");
        }
        return displayName;
    }

    private String normalizeAndValidateUsername(String rawUsername) {
        String username = rawUsername == null ? "" : rawUsername.trim().toLowerCase(java.util.Locale.ROOT);
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw invalidConfiguration("BOOTSTRAP_ADMIN_USERNAME");
        }
        return username;
    }

    private LocalDate validateDateOfBirth(LocalDate dateOfBirth) {
        LocalDate today = LocalDate.now(clock);
        if (dateOfBirth == null
                || dateOfBirth.isAfter(today)
                || dateOfBirth.plusYears(MINIMUM_AGE).isAfter(today)) {
            throw invalidConfiguration("BOOTSTRAP_ADMIN_DATE_OF_BIRTH");
        }
        return dateOfBirth;
    }

    private IllegalStateException invalidConfiguration(String propertyName) {
        // Thông báo chỉ nêu tên biến cấu hình, không đưa giá trị nhạy cảm vào exception hoặc log.
        return new IllegalStateException(propertyName + " chưa được cấu hình hợp lệ");
    }
}
