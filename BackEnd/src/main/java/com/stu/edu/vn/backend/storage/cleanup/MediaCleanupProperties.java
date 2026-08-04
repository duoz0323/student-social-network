package com.stu.edu.vn.backend.storage.cleanup;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Cấu hình scheduler cleanup, không hard-code nhịp retry trong nghiệp vụ. */
@Getter
@Setter
@ConfigurationProperties(prefix = "media.cleanup")
public class MediaCleanupProperties {
    private boolean enabled = true;
    private int batchSize = 50;
    private Duration retryDelay = Duration.ofMinutes(5);
}
