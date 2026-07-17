package com.stu.edu.vn.backend.post.service.impl;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.common.util.LikePatternEscaper;
import com.stu.edu.vn.backend.post.dto.response.HashtagSuggestionItemResponse;
import com.stu.edu.vn.backend.post.dto.response.HashtagSuggestionListResponse;
import com.stu.edu.vn.backend.post.entity.Hashtag;
import com.stu.edu.vn.backend.post.mapper.HashtagMapper;
import com.stu.edu.vn.backend.post.repository.HashtagRepository;
import com.stu.edu.vn.backend.post.service.HashtagService;
import com.stu.edu.vn.backend.post.validation.HashtagNormalizer;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Xử lý điều kiện truy cập, chuẩn hóa và truy vấn gợi ý hashtag hiện có.
 */
@Service
public class HashtagServiceImpl implements HashtagService {

    private static final int MIN_KEYWORD_LENGTH = 2;
    private static final int MAX_KEYWORD_LENGTH = 100;

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final HashtagRepository hashtagRepository;
    private final HashtagNormalizer hashtagNormalizer;
    private final HashtagMapper hashtagMapper;

    public HashtagServiceImpl(
            CurrentUserProvider currentUserProvider,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            HashtagRepository hashtagRepository,
            HashtagNormalizer hashtagNormalizer,
            HashtagMapper hashtagMapper
    ) {
        this.currentUserProvider = currentUserProvider;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.hashtagRepository = hashtagRepository;
        this.hashtagNormalizer = hashtagNormalizer;
        this.hashtagMapper = hashtagMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public HashtagSuggestionListResponse getSuggestions(String keyword) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        ensureCurrentUserCanUseSuggestions(currentUserId);

        String normalizedKeyword;
        try {
            normalizedKeyword = hashtagNormalizer.normalizeSuggestionKeyword(keyword);
        } catch (BusinessException exception) {
            if (exception.getErrorCode() == ErrorCode.POST_HASHTAG_TOO_LONG) {
                throw new BusinessException(ErrorCode.HASHTAG_SUGGESTION_KEYWORD_TOO_LONG);
            }
            throw exception;
        }
        int keywordLength = normalizedKeyword.codePointCount(0, normalizedKeyword.length());
        if (keywordLength > MAX_KEYWORD_LENGTH) {
            throw new BusinessException(ErrorCode.HASHTAG_SUGGESTION_KEYWORD_TOO_LONG);
        }
        if (keywordLength < MIN_KEYWORD_LENGTH) {
            // Không truy vấn từ khóa quá ngắn để tránh quét rộng bảng hashtags.
            return response(keyword, normalizedKeyword, false, List.of());
        }

        List<Hashtag> hashtags = hashtagRepository.findSuggestions(LikePatternEscaper.escape(normalizedKeyword));
        boolean exactMatch = hashtagRepository.findByNormalizedName(normalizedKeyword).isPresent();
        List<HashtagSuggestionItemResponse> suggestions = hashtags.stream()
                .map(hashtagMapper::toSuggestionItem)
                .toList();
        return response(keyword, normalizedKeyword, exactMatch, suggestions);
    }

    private void ensureCurrentUserCanUseSuggestions(Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }

        UserProfile profile = userProfileRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        if (profile.getProfileCompletedAt() == null) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_COMPLETED);
        }
    }

    private HashtagSuggestionListResponse response(
            String keyword,
            String normalizedKeyword,
            boolean exactMatch,
            List<HashtagSuggestionItemResponse> suggestions
    ) {
        // Không cho phép Frontend đề xuất tạo mới khi database đã có normalized_name trùng chính xác.
        return new HashtagSuggestionListResponse(
                keyword,
                normalizedKeyword,
                exactMatch,
                suggestions,
                !exactMatch
        );
    }
}
