package com.stu.edu.vn.backend.messaging.support;

import com.stu.edu.vn.backend.messaging.validation.ValidatedMessageImage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

/** Fingerprint canonical chống tái sử dụng clientMessageId với payload khác. */
public final class MessagePayloadFingerprint {
    private MessagePayloadFingerprint() {
    }

    public static String text(Long conversationId, String content) {
        return sha256("TEXT\n" + conversationId + "\n" + content);
    }

    public static String image(Long conversationId, String content, List<ValidatedMessageImage> images) {
        StringBuilder canonical = new StringBuilder("IMAGE\n")
                .append(conversationId).append('\n')
                .append(content == null ? "" : content).append('\n')
                .append(images.size());
        for (ValidatedMessageImage image : images) {
            canonical.append('\n').append(image.actualMimeType())
                    .append('|').append(image.fileSizeBytes())
                    .append('|').append(image.sha256());
        }
        return sha256(canonical.toString());
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 không khả dụng", exception);
        }
    }
}
