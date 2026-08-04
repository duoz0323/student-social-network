package com.stu.edu.vn.backend.messaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.stu.edu.vn.backend.messaging.dto.request.*;
import com.stu.edu.vn.backend.security.CustomUserPrincipal;
import com.stu.edu.vn.backend.user.enums.*;
import com.stu.edu.vn.backend.storage.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.stu.edu.vn.backend.user.service.UserBlockService;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Bộ test MySQL opt-in cho các hàng rào UNIQUE, pair lock, idempotency và marker monotonic.
 * Database test phải được rebuild/migrate trước; class không bao giờ tự chạm production database.
 */
@SpringBootTest(properties = "bootstrap-admin.enabled=false")
@EnabledIfEnvironmentVariable(named = "AUTH_TEST_DB_URL", matches = "jdbc:mysql:.*")
class MessagingConcurrencyMySqlIntegrationTest {
    @Autowired MessagingService messagingService;
    @Autowired UserBlockService userBlockService;
    @Autowired JdbcTemplate jdbc;
    private Fixture fixture;

    @Autowired MessagingImageService messagingImageService;
    @MockitoBean CloudinaryStorageService cloudinaryStorageService;
    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> required("AUTH_TEST_DB_URL"));
        registry.add("spring.datasource.username", () -> required("AUTH_TEST_DB_USERNAME"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("AUTH_TEST_DB_PASSWORD", ""));
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @BeforeEach
    void createFixture() {
        String suffix = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO users(email,email_verified_at,password_hash,role,status) VALUES (?,CURRENT_TIMESTAMP(6),'hash','USER','ACTIVE')",
                "messaging-a-" + suffix + "@example.com");
        Long a = jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class,
                "messaging-a-" + suffix + "@example.com");
        jdbc.update("INSERT INTO users(email,email_verified_at,password_hash,role,status) VALUES (?,CURRENT_TIMESTAMP(6),'hash','USER','ACTIVE')",
                "messaging-b-" + suffix + "@example.com");
        Long b = jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class,
                "messaging-b-" + suffix + "@example.com");
        jdbc.update("INSERT INTO user_profiles(user_id,display_name,date_of_birth,profile_completed_at) VALUES (?, 'A', '2000-01-01', CURRENT_TIMESTAMP(6))", a);
        jdbc.update("INSERT INTO user_profiles(user_id,display_name,date_of_birth,profile_completed_at) VALUES (?, 'B', '2000-01-01', CURRENT_TIMESTAMP(6))", b);
        // B Follow A là đúng hướng để A bắt đầu conversation với B.
        jdbc.update("INSERT INTO follows(follower_id,following_id) VALUES (?,?)", b, a);
        fixture = new Fixture(a, b);
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        if (fixture == null) return;
        jdbc.update("UPDATE conversations SET last_message_id = NULL WHERE participant_low_id IN (?,?) OR participant_high_id IN (?,?)",
                fixture.a(), fixture.b(), fixture.a(), fixture.b());
        jdbc.update("UPDATE conversation_members cm JOIN conversations c ON c.id=cm.conversation_id SET cm.last_read_message_id=NULL WHERE c.participant_low_id IN (?,?) OR c.participant_high_id IN (?,?)",
                fixture.a(), fixture.b(), fixture.a(), fixture.b());
        jdbc.update("DELETE m FROM messages m JOIN conversations c ON c.id=m.conversation_id WHERE c.participant_low_id IN (?,?) OR c.participant_high_id IN (?,?)",
                fixture.a(), fixture.b(), fixture.a(), fixture.b());
        jdbc.update("DELETE FROM conversations WHERE participant_low_id IN (?,?) OR participant_high_id IN (?,?)",
                fixture.a(), fixture.b(), fixture.a(), fixture.b());
        jdbc.update("DELETE FROM user_blocks WHERE blocker_id IN (?,?) OR blocked_id IN (?,?)",
                fixture.a(), fixture.b(), fixture.a(), fixture.b());
        jdbc.update("DELETE FROM follows WHERE follower_id IN (?,?) OR following_id IN (?,?)",
                fixture.a(), fixture.b(), fixture.a(), fixture.b());
        jdbc.update("DELETE FROM user_profiles WHERE user_id IN (?,?)", fixture.a(), fixture.b());
        jdbc.update("DELETE FROM users WHERE id IN (?,?)", fixture.a(), fixture.b());
    }

    @Test
    void concurrentOpenCreatesOneConversationWithExactlyTwoMembers() throws Exception {
        List<Long> ids = runTwice(() -> asUser(fixture.a(),
                () -> messagingService.openDirectConversation(fixture.b()).conversationId()));
        assertThat(ids.get(0)).isEqualTo(ids.get(1));
        assertThat(count("SELECT COUNT(*) FROM conversations WHERE id=?", ids.get(0))).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM conversation_members WHERE conversation_id=?", ids.get(0))).isEqualTo(2);
    }

    @Test
    void concurrentRetryCreatesOneMessageAndOneReplay() throws Exception {
        Long conversationId = asUser(fixture.a(),
                () -> messagingService.openDirectConversation(fixture.b()).conversationId());
        String key = UUID.randomUUID().toString();
        var responses = runTwice(() -> asUser(fixture.a(),
                () -> messagingService.sendMessage(conversationId, new SendMessageRequest(key, "hello"))));
        assertThat(responses).extracting(item -> item.replayed()).containsExactlyInAnyOrder(false, true);
        assertThat(count("SELECT COUNT(*) FROM messages WHERE sender_id=? AND client_message_id=?",
                fixture.a(), key)).isEqualTo(1);
    }

    @Test
    void concurrentMarkReadCannotMoveMarkerBackward() throws Exception {
        Long conversationId = asUser(fixture.a(),
                () -> messagingService.openDirectConversation(fixture.b()).conversationId());
        Long first = asUser(fixture.a(), () -> messagingService.sendMessage(conversationId,
                new SendMessageRequest(UUID.randomUUID().toString(), "one")).message().messageId());

        Long second = asUser(fixture.a(), () -> messagingService.sendMessage(conversationId,
                new SendMessageRequest(UUID.randomUUID().toString(), "two")).message().messageId());
        runTwiceWithValues(List.of(first, second), marker -> asUser(fixture.b(), () ->
                messagingService.markRead(conversationId, new MarkConversationReadRequest(marker))));
        assertThat(jdbc.queryForObject("SELECT last_read_message_id FROM conversation_members WHERE conversation_id=? AND user_id=?",
                Long.class, conversationId, fixture.b())).isEqualTo(second);
    }

    @Test
    void concurrentImageRetryCreatesOneMessageAndCleansLoserUpload() throws Exception {
        Long conversationId = asUser(fixture.a(),
                () -> messagingService.openDirectConversation(fixture.b()).conversationId());
        String key = UUID.randomUUID().toString();
        byte[] png = Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");
        CountDownLatch bothUploaded = new CountDownLatch(2);
        AtomicInteger assetSequence = new AtomicInteger();
        when(cloudinaryStorageService.uploadMessageImage(any())).thenAnswer(invocation -> {
            int index = assetSequence.incrementAndGet();
            bothUploaded.countDown();
            assertThat(bothUploaded.await(10, TimeUnit.SECONDS)).isTrue();
            return new CloudinaryUploadResult(null, "race-asset-" + index, "image/png", (long) png.length, 1, 1);
        });
        var request = new SendImageMessageRequest(key, "caption", List.of(
                new MockMultipartFile("images", "pixel.png", "image/png", png)));

        var responses = runTwice(() -> asUser(fixture.a(),
                () -> messagingImageService.sendImageMessage(conversationId, request)));

        assertThat(responses).extracting(item -> item.replayed()).containsExactlyInAnyOrder(false, true);
        assertThat(count("SELECT COUNT(*) FROM messages WHERE sender_id=? AND client_message_id=?",
                fixture.a(), key)).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM message_attachments ma JOIN messages m ON m.id=ma.message_id "
                + "WHERE m.sender_id=? AND m.client_message_id=?", fixture.a(), key)).isEqualTo(1);
        verify(cloudinaryStorageService, times(2)).uploadMessageImage(any());
        verify(cloudinaryStorageService, times(1)).deleteMessageImage(startsWith("race-asset-"));
    }

    @Test
    void databaseRejectsDuplicatePairAndSenderOutsideMembership() {
        Long conversationId = asUser(fixture.a(),
                () -> messagingService.openDirectConversation(fixture.b()).conversationId());
        assertThatThrownBySql(() -> jdbc.update("INSERT INTO conversations(participant_low_id,participant_high_id) VALUES (?,?)",
                Math.min(fixture.a(), fixture.b()), Math.max(fixture.a(), fixture.b())));
        assertThatThrownBySql(() -> jdbc.update("INSERT INTO messages(conversation_id,sender_id,client_message_id,type,content,payload_fingerprint) VALUES (?,?,?,'TEXT','invalid',?)",
                conversationId, Long.MAX_VALUE, UUID.randomUUID().toString(), "0".repeat(64)));
    }

    @Test
    void blockAndSendAreSerializedByTheSamePairLock() throws Exception {
        Long conversationId = asUser(fixture.a(),
                () -> messagingService.openDirectConversation(fixture.b()).conversationId());
        asUser(fixture.a(), () -> messagingService.sendMessage(conversationId,
                new SendMessageRequest(UUID.randomUUID().toString(), "first")));
        long before = count("SELECT COUNT(*) FROM messages WHERE conversation_id=?", conversationId);
        List<String> outcomes = runTwiceWithValues(List.of("send", "block"), action -> {
            if (action.equals("block")) {
                return asUser(fixture.b(), () -> { userBlockService.block(fixture.a()); return "BLOCK_OK"; });
            }
            return asUser(fixture.a(), () -> {
                try {
                    messagingService.sendMessage(conversationId,
                            new SendMessageRequest(UUID.randomUUID().toString(), "racing"));
                    return "SEND_OK";
                } catch (com.stu.edu.vn.backend.common.exception.BusinessException exception) {
                    return "SEND_BLOCKED";
                }
            });
        });
        assertThat(outcomes).contains("BLOCK_OK").anyMatch(value -> value.startsWith("SEND_"));
        assertThat(count("SELECT COUNT(*) FROM user_blocks WHERE blocker_id=? AND blocked_id=?",
                fixture.b(), fixture.a())).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM messages WHERE conversation_id=?", conversationId))
                .isBetween(before, before + 1);
    }

    @Test
    void blockAndOpenNeverLeaveConversationWithOneMember() throws Exception {
        List<String> outcomes = runTwiceWithValues(List.of("open", "block"), action -> {
            if (action.equals("block")) {
                return asUser(fixture.b(), () -> { userBlockService.block(fixture.a()); return "BLOCK_OK"; });
            }
            return asUser(fixture.a(), () -> {
                try {
                    messagingService.openDirectConversation(fixture.b());
                    return "OPEN_OK";
                } catch (com.stu.edu.vn.backend.common.exception.BusinessException exception) {
                    return "OPEN_BLOCKED";
                }
            });
        });
        assertThat(outcomes).contains("BLOCK_OK").anyMatch(value -> value.startsWith("OPEN_"));
        List<Long> conversationIds = jdbc.queryForList("""
                SELECT id FROM conversations
                WHERE participant_low_id=? AND participant_high_id=?
                """, Long.class, Math.min(fixture.a(), fixture.b()), Math.max(fixture.a(), fixture.b()));
        for (Long id : conversationIds) {
            assertThat(count("SELECT COUNT(*) FROM conversation_members WHERE conversation_id=?", id)).isEqualTo(2);
        }
    }

    private <T> List<T> runTwice(Callable<T> action) throws Exception {
        return runTwiceWithValues(List.of(0, 1), ignored -> action.call());
    }

    private <I, T> List<T> runTwiceWithValues(List<I> values, ThrowingFunction<I, T> action) throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            List<Future<T>> futures = values.stream().map(value -> executor.submit(() -> {
                ready.countDown();
                start.await(10, TimeUnit.SECONDS);
                return action.apply(value);
            })).toList();
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            return List.of(futures.get(0).get(30, TimeUnit.SECONDS), futures.get(1).get(30, TimeUnit.SECONDS));
        } finally {
            start.countDown();
        }
    }

    private <T> T asUser(Long userId, Callable<T> action) {
        var principal = new CustomUserPrincipal(userId, UserRole.USER, UserStatus.ACTIVE);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()));
        try { return action.call(); }
        catch (RuntimeException exception) { throw exception; }
        catch (Exception exception) { throw new IllegalStateException(exception); }
        finally { SecurityContextHolder.clearContext(); }
    }

    private long count(String sql, Object... args) {
        return jdbc.queryForObject(sql, Long.class, args);
    }

    private void assertThatThrownBySql(Runnable action) {
        org.assertj.core.api.Assertions.assertThatThrownBy(action::run)
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalStateException("Thiếu biến " + name);
        return value;
    }

    private record Fixture(Long a, Long b) { }
    @FunctionalInterface private interface ThrowingFunction<I, T> { T apply(I value) throws Exception; }
}
