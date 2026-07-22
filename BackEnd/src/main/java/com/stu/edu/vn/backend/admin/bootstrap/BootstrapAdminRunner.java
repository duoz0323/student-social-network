package com.stu.edu.vn.backend.admin.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Kích hoạt bootstrap sau khi Spring đã khởi tạo đầy đủ bean và kết nối dữ liệu.
 */
@Component
public class BootstrapAdminRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapAdminRunner.class);

    private final BootstrapAdminService bootstrapAdminService;

    public BootstrapAdminRunner(BootstrapAdminService bootstrapAdminService) {
        this.bootstrapAdminService = bootstrapAdminService;
    }

    @Override
    public void run(ApplicationArguments args) {
        BootstrapAdminResult result = bootstrapAdminService.bootstrapIfEnabled();
        if (result == BootstrapAdminResult.CREATED) {
            LOGGER.info("Đã tạo tài khoản quản trị bootstrap");
        } else if (result == BootstrapAdminResult.ALREADY_EXISTS) {
            LOGGER.info("Bỏ qua bootstrap vì tài khoản cấu hình đã tồn tại");
        }
        // Trạng thái DISABLED không ghi log để ứng dụng khởi động yên lặng theo cấu hình mặc định.
    }
}
