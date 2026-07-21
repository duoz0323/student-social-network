package com.stu.edu.vn.backend.auth.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** Executor giới hạn riêng để delivery không chiếm luồng HTTP hoặc tạo hàng đợi vô hạn. */
@Configuration
@EnableAsync
public class PasswordRecoveryAsyncConfig {
    @Bean(name = "passwordRecoveryExecutor")
    public Executor passwordRecoveryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("password-recovery-");
        executor.initialize();
        return executor;
    }
}
