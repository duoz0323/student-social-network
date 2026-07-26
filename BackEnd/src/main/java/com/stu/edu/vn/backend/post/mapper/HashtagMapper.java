package com.stu.edu.vn.backend.post.mapper;

import com.stu.edu.vn.backend.post.dto.response.HashtagSuggestionItemResponse;
import com.stu.edu.vn.backend.post.entity.Hashtag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper giới hạn các trường hashtag được phép trả cho chức năng autocomplete.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface HashtagMapper {

    @Mapping(target = "hashtagId", source = "id")
    @Mapping(target = "name", source = "displayName")
    HashtagSuggestionItemResponse toSuggestionItem(Hashtag hashtag);
}
