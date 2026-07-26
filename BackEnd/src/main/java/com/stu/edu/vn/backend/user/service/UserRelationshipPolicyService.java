package com.stu.edu.vn.backend.user.service;

/** Chính sách quan hệ dùng chung cho mọi module có tương tác giữa hai người dùng. */
public interface UserRelationshipPolicyService {
    boolean existsBlockEitherDirection(Long userAId, Long userBId);
    boolean hasBlocked(Long blockerId, Long blockedId);
    void assertNoBlock(Long userAId, Long userBId);
}
