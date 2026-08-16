package com.stu.edu.vn.backend.security;

/** Cổng đọc hợp quyền của admin tại thời điểm phát hành Access Token. */
public interface AdminAuthorityResolver {
    AdminAuthorization resolve(Long adminId);
}
