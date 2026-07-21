package com.stu.edu.vn.backend.auth.facebook;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.stu.edu.vn.backend.auth.config.FacebookAuthProperties;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;

/** Adapter Graph API; không ghi token, App Access Token hay response thô vào log/exception. */
@Component
public class OfficialFacebookAccessTokenVerifier implements FacebookAccessTokenVerifier {
    private final FacebookAuthProperties properties;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final HttpClient httpClient;

    public OfficialFacebookAccessTokenVerifier(FacebookAuthProperties properties, ObjectMapper objectMapper, Clock clock) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.httpClient = HttpClient.newBuilder().connectTimeout(properties.getConnectTimeout()).build();
    }

    @Override
    public VerifiedFacebookIdentity verify(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_FACEBOOK_TOKEN_REQUIRED);
        }
        requireConfiguration();
        JsonNode debug = getJson("/debug_token?input_token=" + encode(accessToken)
                + "&access_token=" + encode(properties.getAppId() + "|" + properties.getAppSecret()));
        JsonNode data = debug.path("data");
        if (!data.path("is_valid").asBoolean(false)) {
            throw new BusinessException(ErrorCode.AUTH_FACEBOOK_TOKEN_INVALID);
        }
        if (!properties.getAppId().equals(data.path("app_id").asText())) {
            throw new BusinessException(ErrorCode.AUTH_FACEBOOK_APP_INVALID);
        }
        String userId = requiredText(data, "user_id", ErrorCode.AUTH_FACEBOOK_USER_ID_MISSING);
        long expiresAt = data.path("expires_at").asLong(0);
        if (expiresAt <= Instant.now(clock).getEpochSecond()) {
            throw new BusinessException(ErrorCode.AUTH_FACEBOOK_TOKEN_EXPIRED);
        }
        JsonNode user = getJson("/me?fields=id,name,email,picture.type(large)&access_token=" + encode(accessToken));
        String graphUserId = requiredText(user, "id", ErrorCode.AUTH_FACEBOOK_USER_ID_MISSING);
        if (!userId.equals(graphUserId)) {
            throw new BusinessException(ErrorCode.AUTH_FACEBOOK_TOKEN_INVALID);
        }
        return new VerifiedFacebookIdentity(userId, optionalText(user, "email"), optionalText(user, "name"),
                user.path("picture").path("data").path("url").textValue(), properties.getAppId(), Instant.ofEpochSecond(expiresAt));
    }

    private JsonNode getJson(String pathAndQuery) {
        try {
            String base = properties.getBaseUrl().replaceAll("/+$", "");
            String version = properties.getGraphApiVersion().replaceAll("^/+|/+$", "");
            HttpRequest request = HttpRequest.newBuilder(URI.create(base + "/" + version + pathAndQuery))
                    .timeout(properties.getReadTimeout()).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 500) throw new BusinessException(ErrorCode.AUTH_FACEBOOK_UNAVAILABLE);
            if (response.statusCode() >= 400) throw new BusinessException(ErrorCode.AUTH_FACEBOOK_TOKEN_INVALID);
            return objectMapper.readTree(response.body());
        } catch (BusinessException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.AUTH_FACEBOOK_UNAVAILABLE);
        } catch (IOException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.AUTH_FACEBOOK_UNAVAILABLE);
        }
    }

    private void requireConfiguration() {
        if (properties.getAppId() == null || properties.getAppId().isBlank()
                || properties.getAppSecret() == null || properties.getAppSecret().isBlank()) {
            throw new BusinessException(ErrorCode.AUTH_FACEBOOK_AUTHENTICATION_FAILED);
        }
    }
    private String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
    private String optionalText(JsonNode node, String field) { String value = node.path(field).textValue(); return value == null || value.isBlank() ? null : value; }
    private String requiredText(JsonNode node, String field, ErrorCode code) { String value = optionalText(node, field); if (value == null) throw new BusinessException(code); return value; }
}
