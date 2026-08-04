package com.stu.edu.vn.backend.user.service;

/** Cung cấp cùng một thứ tự khóa cho mọi nghiệp vụ thay đổi quan hệ của một cặp user. */
public interface UserPairLockCoordinator {
    void lockPair(Long firstUserId, Long secondUserId);
}
