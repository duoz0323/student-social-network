package com.stu.edu.vn.backend.messaging.service.impl;

import com.stu.edu.vn.backend.common.exception.*;
import com.stu.edu.vn.backend.messaging.dto.response.MessageAttachmentAccessResponse;
import com.stu.edu.vn.backend.messaging.entity.*;
import com.stu.edu.vn.backend.messaging.repository.*;
import com.stu.edu.vn.backend.messaging.service.MessageAttachmentAccessService;
import com.stu.edu.vn.backend.security.CurrentUserProvider;
import com.stu.edu.vn.backend.storage.*;
import com.stu.edu.vn.backend.user.entity.User;
import com.stu.edu.vn.backend.user.enums.*;
import com.stu.edu.vn.backend.user.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Kiểm tra account, onboarding, membership và Block trước mỗi lần cấp URL. */
@Service
@RequiredArgsConstructor
public class MessageAttachmentAccessServiceImpl implements MessageAttachmentAccessService {
    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserBlockRepository blockRepository;
    private final ConversationMemberRepository memberRepository;
    private final MessageAttachmentRepository attachmentRepository;
    private final CloudinaryStorageService storageService;

    @Override
    @Transactional(readOnly = true)
    public MessageAttachmentAccessResponse createAccess(Long attachmentId) {
        Long userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_ATTACHMENT_NOT_FOUND));
        if (user.getRole() != UserRole.USER || user.getStatus() != UserStatus.ACTIVE
                || !profileRepository.existsByUserIdAndProfileCompletedAtIsNotNull(userId)) {
            throw new BusinessException(ErrorCode.MESSAGING_NOT_ALLOWED);
        }
        MessageAttachment attachment = attachmentRepository.findForAccess(attachmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_ATTACHMENT_NOT_FOUND));
        Conversation conversation = attachment.getMessage().getConversation();
        if (!memberRepository.existsByIdConversationIdAndIdUserId(conversation.getId(), userId)) {
            throw new BusinessException(ErrorCode.MESSAGE_ATTACHMENT_NOT_FOUND);
        }
        Long otherUserId = conversation.otherParticipantId(userId);
        if (blockRepository.existsEitherDirection(userId, otherUserId)) {
            throw new BusinessException(ErrorCode.DIRECT_MESSAGE_NOT_ALLOWED);
        }
        CloudinaryAccessResult access = storageService.createMessageImageAccess(
                attachment.getStoragePublicId(), attachment.getMimeType());
        return new MessageAttachmentAccessResponse(attachment.getId(), access.url(), access.expiresAt());
    }
}
