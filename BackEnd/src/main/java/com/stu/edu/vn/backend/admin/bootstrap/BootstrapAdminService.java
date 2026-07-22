package com.stu.edu.vn.backend.admin.bootstrap;

import com.stu.edu.vn.backend.auth.support.IdentifierNormalizer;
import com.stu.edu.vn.backend.auth.support.IdentifierType;
import com.stu.edu.vn.backend.auth.support.NormalizedIdentifier;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserRole;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$"
    );

    private final BootstrapAdminProperties properties;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public BootstrapAdminService(
            BootstrapAdminProperties properties,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            PasswordEncoder passwordEncoder,
            Clock clock
    ) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Transactional
    public BootstrapAdminResult bootstrapIfEnabled() {
        if (!properties.isEnabled()) {
            return BootstrapAdminResult.DISABLED;
        }

        String normalizedEmail = normalizeAndValidateEmail(properties.getEmail());
        String password = validatePassword(properties.getPassword());
        String displayName = normalizeAndValidateDisplayName(properties.getDisplayName());

        // Email tồn tại chỉ làm bootstrap bỏ qua, tuyệt đối không nâng role hoặc đổi mật khẩu tài khoản cũ.
        if (userRepository.existsByEmail(normalizedEmail)) {
            return BootstrapAdminResult.ALREADY_EXISTS;
        }

        User admin = new User(normalizedEmail, null, passwordEncoder.encode(password));
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setBlockedAt(null);
        admin.setBlockedReason(null);
        User savedAdmin = userRepository.saveAndFlush(admin);

        UserProfile profile = new UserProfile(savedAdmin);
        profile.setDisplayName(displayName);
        profile.setAvatarUrl(null);
        profile.setAvatarPublicId(null);
        profile.setBio(null);
        profile.setDateOfBirth(null);
        profile.setProfileCompletedAt(LocalDateTime.now(clock));
        // saveAndFlush làm lỗi hồ sơ xuất hiện ngay trong transaction để bản ghi users được rollback cùng lúc.
        userProfileRepository.saveAndFlush(profile);

        return BootstrapAdminResult.CREATED;
    }

    private String normalizeAndValidateEmail(String rawEmail) {
        try {
            NormalizedIdentifier identifier = IdentifierNormalizer.normalize(rawEmail);
            if (identifier.type() != IdentifierType.EMAIL) {
                throw invalidConfiguration("BOOTSTRAP_ADMIN_EMAIL");
            }
            return identifier.value();
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

    private IllegalStateException invalidConfiguration(String propertyName) {
        // Thông báo chỉ nêu tên biến cấu hình, không đưa giá trị nhạy cảm vào exception hoặc log.
        return new IllegalStateException(propertyName + " chưa được cấu hình hợp lệ");
    }
}
