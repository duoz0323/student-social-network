package com.stu.edu.vn.backend.messaging.entity;

import com.stu.edu.vn.backend.messaging.enums.MessageAttachmentMediaType;
import com.stu.edu.vn.backend.messaging.enums.StorageProvider;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Metadata ảnh chat; URL truy cập được cấp riêng sau khi kiểm tra quyền. */
@Entity
@Table(name = "message_attachments", uniqueConstraints = {
        @UniqueConstraint(name = "uq_message_attachments_message_order", columnNames = {"message_id", "display_order"}),
        @UniqueConstraint(name = "uq_message_attachments_storage_asset", columnNames = {"storage_provider", "storage_public_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 16)
    private MessageAttachmentMediaType mediaType;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_provider", nullable = false, length = 32)
    private StorageProvider storageProvider;

    @Column(name = "storage_public_id", nullable = false, length = 255)
    private String storagePublicId;

    @Column(name = "mime_type", nullable = false, length = 64)
    private String mimeType;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "width", nullable = false)
    private Integer width;

    @Column(name = "height", nullable = false)
    private Integer height;

    @Column(name = "display_order", nullable = false)
    private Byte displayOrder;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public MessageAttachment(Message message, String storagePublicId, String mimeType, long fileSizeBytes,
                             int width, int height, int displayOrder) {
        this.message = message;
        this.mediaType = MessageAttachmentMediaType.IMAGE;
        this.storageProvider = StorageProvider.CLOUDINARY;
        this.storagePublicId = storagePublicId;
        this.mimeType = mimeType;
        this.fileSizeBytes = fileSizeBytes;
        this.width = width;
        this.height = height;
        this.displayOrder = (byte) displayOrder;
    }
}
