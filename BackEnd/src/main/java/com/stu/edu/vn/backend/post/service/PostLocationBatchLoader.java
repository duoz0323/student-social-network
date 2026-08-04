package com.stu.edu.vn.backend.post.service;

import com.stu.edu.vn.backend.location.entity.Location;
import com.stu.edu.vn.backend.location.repository.LocationRepository;
import com.stu.edu.vn.backend.post.entity.Post;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Batch-load Location cho một trang Post để không khởi tạo lazy proxy trong vòng lặp. */
@Component
@RequiredArgsConstructor
public class PostLocationBatchLoader {
    private final LocationRepository locationRepository;

    public Map<Long, Location> loadByPostId(List<Post> posts) {
        List<Long> locationIds = posts.stream()
                .map(Post::getLocation)
                .filter(java.util.Objects::nonNull)
                .map(Location::getId)
                .distinct()
                .toList();
        if (locationIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Location> byId = locationRepository.findAllById(locationIds).stream()
                .collect(Collectors.toMap(Location::getId, Function.identity()));
        return posts.stream()
                .filter(post -> post.getLocation() != null)
                .filter(post -> byId.containsKey(post.getLocation().getId()))
                .collect(Collectors.toMap(Post::getId, post -> byId.get(post.getLocation().getId())));
    }
}
