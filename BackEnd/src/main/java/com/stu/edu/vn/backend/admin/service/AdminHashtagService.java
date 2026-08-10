package com.stu.edu.vn.backend.admin.service;

import com.stu.edu.vn.backend.admin.dto.response.AdminHashtagListItemResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminHashtagDeleteResponse;
import com.stu.edu.vn.backend.admin.dto.response.AdminHashtagUpdateResponse;
import com.stu.edu.vn.backend.common.api.PageResponse;

/** Use case đọc danh sách hashtag dành riêng cho ADMIN. */
public interface AdminHashtagService {
    PageResponse<AdminHashtagListItemResponse> getHashtags(String keyword, int page, int size);

    AdminHashtagListItemResponse createHashtag(String name);

    AdminHashtagDeleteResponse deleteHashtag(Long hashtagId);

    AdminHashtagUpdateResponse updateHashtag(Long hashtagId, String name);
}
