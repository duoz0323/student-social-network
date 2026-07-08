package com.stu.edu.vn.backend.post.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Entity post_media chỉ lưu metadata và URL ảnh, không lưu file ảnh dạng BLOB.
 */
@Entity
@Table(name = "post_media")
public class PostMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Khóa chính tự tăng của ảnh bài viết.
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    // Bài viết sở hữu media, dùng LAZY để không tải bài khi chỉ cần metadata ảnh.
    private Post post;

    @Column(name = "media_url", nullable = false, length = 1000)
    // URL ảnh trên Cloud Storage/Cloudinary, database không lưu file nhị phân.
    private String mediaUrl;

    @Column(name = "storage_public_id", nullable = false, length = 255, unique = true)
    // Public ID hoặc object key dùng để quản lý/xóa file trên dịch vụ lưu trữ.
    private String storagePublicId;

    @Column(name = "mime_type", nullable = false, length = 20)
    // MIME type thực tế đã được Backend kiểm tra trước khi lưu metadata.
    private String mimeType;

    @Column(name = "file_size_bytes", nullable = false)
    // Kích thước file theo byte, phục vụ kiểm tra và hiển thị metadata nếu cần.
    private Long fileSizeBytes;

    @Column(name = "width_px")
    // Chiều rộng ảnh theo pixel, có thể NULL nếu dịch vụ lưu trữ không trả metadata này.
    private Integer widthPx;

    @Column(name = "height_px")
    // Chiều cao ảnh theo pixel, có thể NULL nếu dịch vụ lưu trữ không trả metadata này.
    private Integer heightPx;

    @Column(name = "display_order", nullable = false)
    // Thứ tự hiển thị ảnh trong bài, schema giới hạn từ 0 đến 3.
    private Byte displayOrder;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    // Thời điểm tạo metadata ảnh do MySQL tự gán.
    private LocalDateTime createdAt;

    protected PostMedia() {
        // Constructor rỗng dành cho JPA.
    }

    public PostMedia(
            Post post,
            String mediaUrl,
            String storagePublicId,
            String mimeType,
            Long fileSizeBytes,
            Integer displayOrder
    ) {
        // Constructor nghiệp vụ tối thiểu cho ảnh bài viết sau khi upload lên Cloud Storage.
        this.post = post;
        this.mediaUrl = mediaUrl;
        this.storagePublicId = storagePublicId;
        this.mimeType = mimeType;
        this.fileSizeBytes = fileSizeBytes;
        this.displayOrder = displayOrder == null ? null : displayOrder.byteValue();
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getStoragePublicId() {
        return storagePublicId;
    }

    public void setStoragePublicId(String storagePublicId) {
        this.storagePublicId = storagePublicId;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public Integer getWidthPx() {
        return widthPx;
    }

    public void setWidthPx(Integer widthPx) {
        this.widthPx = widthPx;
    }

    public Integer getHeightPx() {
        return heightPx;
    }

    public void setHeightPx(Integer heightPx) {
        this.heightPx = heightPx;
    }

    public Integer getDisplayOrder() {
        return displayOrder == null ? null : displayOrder.intValue();
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder == null ? null : displayOrder.byteValue();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
