package com.stu.edu.vn.backend.admin.collaborator.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Username bất biến sau khi tạo; API này chỉ cho sửa nội dung hồ sơ công khai còn lại. */
public record UpdateManagedSocialIdentityRequest(
        @NotBlank @Size(max = 100) String displayName,
        @Size(max = 500) String bio
) { }
