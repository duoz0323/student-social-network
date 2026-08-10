package com.stu.edu.vn.backend.admin.bootstrap;

import java.time.LocalDate;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Ánh xạ cấu hình bootstrap ADMIN từ biến môi trường, không chứa giá trị bí mật mặc định.
 */
@ConfigurationProperties(prefix = "bootstrap-admin")
public class BootstrapAdminProperties {

    private boolean enabled;
    private String email;
    private String password;
    private String username = "system_admin";
    private String displayName = "Quản trị viên";
    private LocalDate dateOfBirth = LocalDate.of(2000, 1, 1);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
