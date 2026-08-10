package com.stu.edu.vn.backend.post.dto.response;

/**
 * Thong tin goc nhin cua nguoi dang xem bai viet, chi chua cac co trang thai phuc vu hien thi UI.
 */
public record PostViewerResponse(
        boolean owner,
        boolean likedByCurrentUser
) {
    public PostViewerResponse(boolean owner) {
        this(owner, false);
    }
}
