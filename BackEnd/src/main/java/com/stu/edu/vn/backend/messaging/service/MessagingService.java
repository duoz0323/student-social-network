package com.stu.edu.vn.backend.messaging.service;

import com.stu.edu.vn.backend.common.api.CursorPageResponse;
import com.stu.edu.vn.backend.messaging.dto.request.*;
import com.stu.edu.vn.backend.messaging.dto.response.*;
import com.stu.edu.vn.backend.common.api.PageResponse;

/** REST Core contract của Messaging; realtime không thuộc service giai đoạn 1B. */
public interface MessagingService {
    CursorPageResponse<ConversationResponse> getConversations(int limit, String cursor);
    DirectConversationResponse openDirectConversation(Long recipientUserId);
    CursorPageResponse<MessageResponse> getMessages(Long conversationId, int limit, String cursor);
    SendMessageResponse sendMessage(Long conversationId, SendMessageRequest request);
    MarkConversationReadResponse markRead(Long conversationId, MarkConversationReadRequest request);
    MessagingUnreadCountResponse getUnreadCount();
    PageResponse<ShareRecipientResponse> getShareRecipients(String keyword, int page, int size);
}
