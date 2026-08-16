package com.stu.edu.vn.backend.moderation.client;

import com.stu.edu.vn.backend.moderation.config.ContentModerationProperties;
import com.stu.edu.vn.backend.moderation.dto.ModerationAssessment;
import com.stu.edu.vn.backend.moderation.enums.ModerationClassification;
import com.stu.edu.vn.backend.moderation.exception.ModerationProviderException;
import com.stu.edu.vn.backend.moderation.provider.ModerationProvider;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Adapter gọi FastAPI nội bộ; không ghi log nguyên văn nội dung hoặc response inference. */
@Component
public class LocalAiModerationProvider implements ModerationProvider {
    private static final String[] REQUIRED_LABELS = {"CLEAN", "OFFENSIVE", "HATE"};

    private final ContentModerationProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    public LocalAiModerationProvider(ContentModerationProperties properties, ObjectMapper objectMapper) {
        this(properties, objectMapper, buildHttpClient(properties));
    }

    static HttpClient buildHttpClient(ContentModerationProperties properties) {
        return HttpClient.newBuilder()
                // Uvicorn phục vụ HTTP/1.1; không thử h2c để tránh request body bị mất khi upgrade.
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(properties.getConnectTimeout())
                .build();
    }

    LocalAiModerationProvider(
            ContentModerationProperties properties,
            ObjectMapper objectMapper,
            HttpClient httpClient
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    @Override
    public ModerationAssessment moderate(String text) {
        requireConfiguration();
        try {
            String baseUrl = properties.getLocalBaseUrl().replaceAll("/+$", "");
            String body = objectMapper.writeValueAsString(Map.of("text", text));
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/v1/moderation"))
                    .timeout(properties.getReadTimeout())
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body.getBytes(StandardCharsets.UTF_8)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ModerationProviderException(
                        "Local AI provider trả HTTP không thành công: " + response.statusCode());
            }
            return parseResponse(response.body());
        } catch (ModerationProviderException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ModerationProviderException("Local AI moderation request bị gián đoạn", exception);
        } catch (IOException | IllegalArgumentException exception) {
            throw new ModerationProviderException("Không thể gọi local AI moderation provider", exception);
        }
    }

    ModerationAssessment parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode labelNode = root.path("label");
            JsonNode confidenceNode = root.path("confidence");
            JsonNode scoresNode = root.path("scores");
            if (!labelNode.isTextual() || !confidenceNode.isNumber() || !scoresNode.isObject()) {
                throw new ModerationProviderException("Local AI response sai cấu trúc");
            }

            ModerationClassification classification;
            try {
                classification = ModerationClassification.valueOf(labelNode.asText());
            } catch (IllegalArgumentException exception) {
                throw new ModerationProviderException("Local AI response chứa label không hỗ trợ", exception);
            }
            double confidence = requireProbability(confidenceNode, "confidence");
            double selectedScore = 0d;
            for (String label : REQUIRED_LABELS) {
                double score = requireProbability(scoresNode.path(label), "scores." + label);
                if (label.equals(classification.name())) {
                    selectedScore = score;
                }
            }
            if (Math.abs(selectedScore - confidence) > 0.000001d) {
                throw new ModerationProviderException("Local AI confidence không khớp score của label");
            }
            return ModerationAssessment.fromLocalModel(classification, confidence);
        } catch (ModerationProviderException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ModerationProviderException("Local AI response không hợp lệ", exception);
        }
    }

    private double requireProbability(JsonNode node, String field) {
        if (!node.isNumber()) {
            throw new ModerationProviderException("Local AI response thiếu " + field);
        }
        double value = node.asDouble();
        if (!Double.isFinite(value) || value < 0d || value > 1d) {
            throw new ModerationProviderException("Local AI probability nằm ngoài miền hợp lệ");
        }
        return value;
    }

    private void requireConfiguration() {
        if (properties.getLocalBaseUrl() == null || properties.getLocalBaseUrl().isBlank()) {
            throw new ModerationProviderException("Local AI provider chưa được cấu hình");
        }
    }
}
