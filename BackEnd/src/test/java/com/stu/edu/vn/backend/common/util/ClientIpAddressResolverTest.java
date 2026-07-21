package com.stu.edu.vn.backend.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class ClientIpAddressResolverTest {

    @Test
    void ignoresSpoofableProxyHeaderByDefault() {
        ClientIpAddressProperties properties = new ClientIpAddressProperties();
        ClientIpAddressResolver resolver = new ClientIpAddressResolver(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.10");
        request.addHeader("X-Forwarded-For", "203.0.113.10");

        assertThat(resolver.resolve(request)).isEqualTo("10.0.0.10");
    }

    @Test
    void readsProxyHeaderOnlyWhenExplicitlyTrusted() {
        ClientIpAddressProperties properties = new ClientIpAddressProperties();
        properties.setTrustProxyHeaders(true);
        ClientIpAddressResolver resolver = new ClientIpAddressResolver(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.10");
        request.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.1");

        assertThat(resolver.resolve(request)).isEqualTo("203.0.113.10");
    }
}
