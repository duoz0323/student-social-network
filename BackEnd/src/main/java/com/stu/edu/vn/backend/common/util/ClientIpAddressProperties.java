package com.stu.edu.vn.backend.common.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Chỉ cho phép đọc header IP do proxy cung cấp khi môi trường triển khai đã cấu hình proxy tin cậy.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "security.client-ip")
public class ClientIpAddressProperties {

    private boolean trustProxyHeaders;
}
