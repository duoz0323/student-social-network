package com.stu.edu.vn.backend.report.service.impl;

import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import com.stu.edu.vn.backend.post.entity.Post;
import com.stu.edu.vn.backend.post.entity.PostMedia;
import com.stu.edu.vn.backend.post.enums.PostStatus;
import com.stu.edu.vn.backend.post.repository.PostRepository;
import com.stu.edu.vn.backend.post.repository.PostMediaRepository;
import com.stu.edu.vn.backend.report.dto.request.CreateReportRequest;
import com.stu.edu.vn.backend.report.dto.response.CreateReportResponse;
import com.stu.edu.vn.backend.report.entity.Report;
import com.stu.edu.vn.backend.report.entity.ModerationCase;
import com.stu.edu.vn.backend.report.enums.ModerationCaseStatus;
import com.stu.edu.vn.backend.report.enums.ReportReason;
import com.stu.edu.vn.backend.report.enums.ReportStatus;
import com.stu.edu.vn.backend.report.mapper.ReportMapper;
import com.stu.edu.vn.backend.report.repository.ReportRepository;
import com.stu.edu.vn.backend.report.repository.ModerationCaseRepository;
import com.stu.edu.vn.backend.report.service.ReportService;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.entity.UserProfile;
import com.stu.edu.vn.backend.user.enums.UserStatus;
import com.stu.edu.vn.backend.user.repository.UserProfileRepository;
import com.stu.edu.vn.backend.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.Comparator;
import java.util.List;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Triển khai tạo báo cáo USER, bao gồm kiểm tra quyền, chống trùng và chụp snapshot trong cùng transaction.
 */
@Service
public class ReportServiceImpl implements ReportService {

    private static final int MAX_DESCRIPTION_LENGTH = 1000;

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final ReportRepository reportRepository;
    private final ModerationCaseRepository moderationCaseRepository;
    private final ReportMapper reportMapper;
    private final EntityManager entityManager;
    private final Clock clock;

    public ReportServiceImpl(
            CurrentUserProvider currentUserProvider,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            PostRepository postRepository,
            PostMediaRepository postMediaRepository,
            ReportRepository reportRepository,
            ModerationCaseRepository moderationCaseRepository,
            ReportMapper reportMapper,
            EntityManager entityManager,
            Clock clock
    ) {
        this.currentUserProvider = currentUserProvider;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.postRepository = postRepository;
        this.postMediaRepository = postMediaRepository;
        this.reportRepository = reportRepository;
        this.moderationCaseRepository = moderationCaseRepository;
        this.reportMapper = reportMapper;
        this.entityManager = entityManager;
        this.clock = clock;
    }

    @Override
    @Transactional
    public CreateReportResponse createPostReport(Long postId, CreateReportRequest request) {
        Long reporterId = currentUserProvider.getCurrentUserId();
        User reporter = ensureCurrentUserCanReport(reporterId);
        ReportReason reason = requireReason(request);
        String description = normalizeDescription(reason, request.description());

        // Khóa Post trước khi tìm/tạo case để hai báo cáo đầu tiên không thể tạo hai case OPEN.
        Post post = postRepository.findReportTargetByIdForUpdate(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        if (post.getStatus() != PostStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.POST_NOT_AVAILABLE);
        }
        if (post.getAuthor().getId().equals(reporterId)) {
            throw new BusinessException(ErrorCode.REPORT_OWN_POST_FORBIDDEN);
        }
        if (reportRepository.existsEffectiveReport(reporterId, postId, ModerationCaseStatus.OPEN)) {
            throw new BusinessException(ErrorCode.REPORT_ALREADY_PENDING);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        ModerationCase moderationCase = moderationCaseRepository
                .findByPost_IdAndStatus(postId, ModerationCaseStatus.OPEN)
                .orElseGet(() -> moderationCaseRepository.save(new ModerationCase(post, now)));

        // Media được tải riêng sau khi đã khóa Post để tránh khóa ngoài ý muốn do fetch join collection.
        String mediaSnapshot = serializeMediaSnapshot(
                postMediaRepository.findByPost_IdOrderByDisplayOrderAsc(postId));
        Report report = new Report(
                reporter,
                post,
                moderationCase,
                reason,
                description,
                post.getContent(),
                mediaSnapshot
        );

        try {
            moderationCase.registerReport(now);
            // Flush cùng lúc để mọi lỗi constraint rollback cả Report và bộ đếm của case.
            reportRepository.saveAndFlush(report);
        } catch (DataIntegrityViolationException exception) {
            // Không để lộ SQL/constraint; mọi race condition báo cáo PENDING trùng đều thành lỗi nghiệp vụ ổn định.
            throw new BusinessException(ErrorCode.REPORT_ALREADY_PENDING);
        }

        // Audit timestamp do MySQL sinh nên refresh trước khi ánh xạ response.
        entityManager.refresh(report);
        return reportMapper.toCreateResponse(report);
    }

    private User ensureCurrentUserCanReport(Long reporterId) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (reporter.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_BLOCKED);
        }

        UserProfile profile = userProfileRepository.findById(reporterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        if (profile.getProfileCompletedAt() == null) {
            throw new BusinessException(ErrorCode.PROFILE_NOT_COMPLETED);
        }
        return reporter;
    }

    private ReportReason requireReason(CreateReportRequest request) {
        if (request == null || request.reason() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        return request.reason();
    }

    private String normalizeDescription(ReportReason reason, String rawDescription) {
        String description = rawDescription == null ? null : rawDescription.trim();
        if (description != null && description.isEmpty()) {
            description = null;
        }
        if (reason == ReportReason.OTHER && description == null) {
            throw new BusinessException(ErrorCode.REPORT_DESCRIPTION_REQUIRED);
        }
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new BusinessException(ErrorCode.REPORT_DESCRIPTION_TOO_LONG);
        }
        return description;
    }

    private String serializeMediaSnapshot(List<PostMedia> media) {
        // Snapshot chỉ lưu URL theo display_order; metadata nội bộ Cloudinary không cần thiết cho bằng chứng MVP.
        List<String> mediaUrls = media.stream()
                .sorted(Comparator.comparing(
                        PostMedia::getDisplayOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .map(PostMedia::getMediaUrl)
                .toList();
        return mediaUrls.stream()
                .map(this::toJsonString)
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));
    }

    private String toJsonString(String value) {
        // Escape đầy đủ ký tự điều khiển để URL luôn tạo thành JSON hợp lệ mà không cần bổ sung thư viện mới.
        StringBuilder escaped = new StringBuilder(value.length() + 2).append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.append('"').toString();
    }
}
