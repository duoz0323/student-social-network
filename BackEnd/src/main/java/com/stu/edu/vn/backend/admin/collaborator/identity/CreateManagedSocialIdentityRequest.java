package com.stu.edu.vn.backend.admin.collaborator.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateManagedSocialIdentityRequest(
        @NotBlank @Size(max = 30) String username,
        @NotBlank @Size(max = 100) String displayName,
        @Size(max = 1000) String avatarUrl,
        @Size(max = 500) String bio
) { }
