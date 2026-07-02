package com.stu.edu.vn.backend.common.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Utility lấy IP Client theo cách thận trọng, ưu tiên header proxy phổ biến nhưng không ghi log dữ liệu nhạy cảm.
 */
@Component
public class ClientIpAddressResolver {

    private static final int MAX_IP_LENGTH = 45;
    private static final Set<String> UNKNOWN_VALUES = Set.of("", "unknown", "null");

    public String resolve(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        // X-Forwarded-For có thể chứa nhiều IP, IP đầu tiên là Client gần Frontend nhất trong chuỗi proxy.
        String forwardedFor = firstHeaderValue(request.getHeader("X-Forwarded-For"));
        if (isUsableIp(forwardedFor)) {
            return truncateIp(forwardedFor);
        }

        // X-Real-IP thường được reverse proxy gán khi chỉ cần truyền một địa chỉ Client.
        String realIp = normalizeSingleIp(request.getHeader("X-Real-IP"));
        if (isUsableIp(realIp)) {
            return truncateIp(realIp);
        }

        // RemoteAddr là giá trị fallback từ servlet container khi không có proxy header đáng dùng.
        return truncateIp(normalizeSingleIp(request.getRemoteAddr()));
    }

    private String firstHeaderValue(String headerValue) {
        if (headerValue == null) {
            return null;
        }
        return normalizeSingleIp(headerValue.split(",")[0]);
    }

    private String normalizeSingleIp(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    private boolean isUsableIp(String value) {
        return value != null && !UNKNOWN_VALUES.contains(value.toLowerCase());
    }

    private String truncateIp(String value) {
        if (value == null || value.length() <= MAX_IP_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_IP_LENGTH);
    }
}
