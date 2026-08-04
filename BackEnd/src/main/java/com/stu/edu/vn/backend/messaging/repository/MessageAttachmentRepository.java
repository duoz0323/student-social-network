package com.stu.edu.vn.backend.messaging.repository;

import com.stu.edu.vn.backend.messaging.entity.MessageAttachment;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Truy vấn attachment theo message để tránh trả URL lưu trữ trực tiếp. */
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, Long> {
    List<MessageAttachment> findByMessageIdInOrderByMessageIdAscDisplayOrderAsc(Collection<Long> messageIds);

    List<MessageAttachment> findByMessageIdOrderByDisplayOrderAsc(Long messageId);

    @Query("""
            select attachment from MessageAttachment attachment
            join fetch attachment.message message
            join fetch message.conversation conversation
            where attachment.id = :attachmentId
            """)
    Optional<MessageAttachment> findForAccess(@Param("attachmentId") Long attachmentId);
}
