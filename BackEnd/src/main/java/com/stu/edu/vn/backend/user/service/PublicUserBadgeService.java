package com.stu.edu.vn.backend.user.service;

import com.stu.edu.vn.backend.admin.collaborator.identity.AdminSocialIdentityRepository;
import com.stu.edu.vn.backend.user.enums.PublicUserBadge;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Resolve badge theo lô để các danh sách public không phát sinh N+1. */
@Service
@RequiredArgsConstructor
public class PublicUserBadgeService {
    private final AdminSocialIdentityRepository identityRepository;

    @Transactional(readOnly = true)
    public List<PublicUserBadge> getBadges(Long userId) {
        return getBadgesByUserIds(List.of(userId)).getOrDefault(userId, List.of());
    }

    @Transactional(readOnly = true)
    public Map<Long, List<PublicUserBadge>> getBadgesByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Map.of();
        Set<Long> activeCollaborators = Set.copyOf(
                identityRepository.findActiveCollaboratorSocialUserIds(userIds));
        return userIds.stream().distinct().collect(Collectors.toMap(Function.identity(),
                id -> activeCollaborators.contains(id)
                        ? List.of(PublicUserBadge.COLLABORATOR) : List.of()));
    }
}
