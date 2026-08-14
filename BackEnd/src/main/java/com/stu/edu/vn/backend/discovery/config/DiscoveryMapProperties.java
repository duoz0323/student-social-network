package com.stu.edu.vn.backend.discovery.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Cấu hình giới hạn Discovery Map tập trung để repository luôn fetch đúng max + 1. */
@ConfigurationProperties(prefix = "discovery.map")
public class DiscoveryMapProperties {
    private int maxLocations = 200;

    public int getMaxLocations() {
        return maxLocations;
    }

    public void setMaxLocations(int maxLocations) {
        this.maxLocations = maxLocations;
    }
}
