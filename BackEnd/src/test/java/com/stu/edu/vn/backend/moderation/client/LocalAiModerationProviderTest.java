package com.stu.edu.vn.backend.moderation.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.stu.edu.vn.backend.moderation.config.ContentModerationProperties;
import com.stu.edu.vn.backend.moderation.enums.ModerationCategory;
import com.stu.edu.vn.backend.moderation.enums.ModerationClassification;
import com.stu.edu.vn.backend.moderation.exception.ModerationProviderException;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class LocalAiModerationProviderTest {
    private final ContentModerationProperties properties = configuredProperties();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parsesCleanOffensiveAndHateResponses() {
        LocalAiModerationProvider provider = provider(mock(HttpClient.class));

        var clean = provider.parseResponse(response("CLEAN", 0.91d));
        var offensive = provider.parseResponse(response("OFFENSIVE", 0.82d));
        var hate = provider.parseResponse(response("HATE", 0.95d));

        assertThat(clean.classification()).isEqualTo(ModerationClassification.CLEAN);
        assertThat(clean.category()).isEqualTo(ModerationCategory.SAFE);
        assertThat(offensive.classification()).isEqualTo(ModerationClassification.OFFENSIVE);
        assertThat(offensive.category()).isEqualTo(ModerationCategory.OFFENSIVE);
        assertThat(hate.classification()).isEqualTo(ModerationClassification.HATE);
        assertThat(hate.category()).isEqualTo(ModerationCategory.HATE);
    }

    @Test
    void rejectsUnknownLabelAndMalformedResponse() {
        LocalAiModerationProvider provider = provider(mock(HttpClient.class));

        assertThatThrownBy(() -> provider.parseResponse(response("UNKNOWN", 0.8d)))
                .isInstanceOf(ModerationProviderException.class);
        assertThatThrownBy(() -> provider.parseResponse("{\"label\":\"CLEAN\"}"))
                .isInstanceOf(ModerationProviderException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void allFourAndFiveHundredStatusesFailClosedAtAdapterBoundary() throws Exception {
        for (int status : new int[]{400, 429, 500, 503}) {
            HttpClient httpClient = mock(HttpClient.class);
            HttpResponse<String> response = mock(HttpResponse.class);
            when(response.statusCode()).thenReturn(status);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

            assertThatThrownBy(() -> provider(httpClient).moderate("Nội dung kiểm thử"))
                    .isInstanceOf(ModerationProviderException.class);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void timeoutOrConnectionFailureFailsClosedAtAdapterBoundary() throws Exception {
        HttpClient timeoutClient = mock(HttpClient.class);
        when(timeoutClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new java.net.http.HttpTimeoutException("timeout"));
        HttpClient connectionClient = mock(HttpClient.class);
        when(connectionClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("connection refused"));

        assertThatThrownBy(() -> provider(timeoutClient).moderate("timeout"))
                .isInstanceOf(ModerationProviderException.class);
        assertThatThrownBy(() -> provider(connectionClient).moderate("connection"))
                .isInstanceOf(ModerationProviderException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendsJsonAsExplicitUtf8Body() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(response("CLEAN", 0.91d));
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenAnswer(invocation -> {
            HttpRequest request = invocation.getArgument(0);
            assertThat(request.headers().firstValue("Content-Type"))
                    .contains("application/json; charset=UTF-8");
            assertThat(request.bodyPublisher()).isPresent();
            assertThat(request.bodyPublisher().orElseThrow().contentLength())
                    .isEqualTo("{\"text\":\"Nội dung kiểm thử\"}".getBytes(StandardCharsets.UTF_8).length);
            return response;
        });

        provider(httpClient).moderate("Nội dung kiểm thử");
    }

    @Test
    void productionClientUsesHttp11ForUvicornCompatibility() {
        assertThat(LocalAiModerationProvider.buildHttpClient(properties).version())
                .isEqualTo(HttpClient.Version.HTTP_1_1);
    }

    private LocalAiModerationProvider provider(HttpClient httpClient) {
        return new LocalAiModerationProvider(properties, objectMapper, httpClient);
    }

    private String response(String label, double confidence) {
        double clean = "CLEAN".equals(label) ? confidence : 0.05d;
        double offensive = "OFFENSIVE".equals(label) ? confidence : 0.10d;
        double hate = "HATE".equals(label) ? confidence : 0.15d;
        return """
                {"label":"%s","confidence":%s,
                 "scores":{"CLEAN":%s,"OFFENSIVE":%s,"HATE":%s}}
                """.formatted(label, confidence, clean, offensive, hate);
    }

    private ContentModerationProperties configuredProperties() {
        ContentModerationProperties configured = new ContentModerationProperties();
        configured.setLocalBaseUrl("http://127.0.0.1:8001");
        return configured;
    }
}
