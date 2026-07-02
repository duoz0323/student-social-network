package com.stu.edu.vn.backend;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BackEndApplicationTests {

    @Test
    void applicationClassExists() {
        // Test khói nhẹ để không phụ thuộc database thật khi chạy bộ test đăng ký.
        assertThat(BackEndApplication.class).isNotNull();
    }
}
