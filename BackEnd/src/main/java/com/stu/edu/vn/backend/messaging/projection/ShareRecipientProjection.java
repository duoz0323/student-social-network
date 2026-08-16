package com.stu.edu.vn.backend.messaging.projection;

/** Projection database-side cho recipient discovery, không tải User Entity. */
public interface ShareRecipientProjection {
    Long getUserId();
    String getUsername();
    String getDisplayName();
    String getAvatarUrl();
    Long getConversationId();
    Integer getExistingConversationPriority();
}
