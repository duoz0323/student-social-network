package com.stu.edu.vn.backend.auth.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.stu.edu.vn.backend.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class AuthEntityMappingTest {

    @Test
    void userNullableAndEnumMappingsMatchSchema() throws Exception {
        assertTrue(field(User.class, "email").getAnnotation(Column.class).nullable());
        assertTrue(field(User.class, "phoneNumber").getAnnotation(Column.class).nullable());
        assertTrue(field(User.class, "passwordHash").getAnnotation(Column.class).nullable());
        assertEnumString(User.class, "role");
        assertEnumString(User.class, "status");
    }

    @Test
    void providerNullableAndEnumMappingsMatchSchema() throws Exception {
        assertEnumString(UserAuthProvider.class, "provider");
        assertTrue(field(UserAuthProvider.class, "providerEmail").getAnnotation(Column.class).nullable());
        assertTrue(field(UserAuthProvider.class, "providerEmailVerified").getAnnotation(Column.class).nullable());
        assertLazyWithoutCascade(UserAuthProvider.class, "user");
    }

    @Test
    void everyChallengeRelationshipIsLazyAndDoesNotCascadePersist() throws Exception {
        assertLazyWithoutCascade(PendingRegistration.class, "completedUser");
        assertLazyWithoutCascade(AuthMethodLinkChallenge.class, "user");
        assertLazyWithoutCascade(SocialAuthChallenge.class, "pendingRegistration");
        assertLazyWithoutCascade(SocialAuthChallenge.class, "conflictingUser");
        assertLazyWithoutCascade(SocialAuthChallenge.class, "resolvedUser");
        assertLazyWithoutCascade(ReauthenticationChallenge.class, "user");
    }

    @Test
    void challengeEntitiesExposeNoPublicSetter() {
        for (Class<?> entityType : new Class<?>[]{
                PendingRegistration.class,
                AuthMethodLinkChallenge.class,
                SocialAuthChallenge.class,
                ReauthenticationChallenge.class
        }) {
            boolean hasPublicSetter = Arrays.stream(entityType.getDeclaredMethods())
                    .anyMatch(this::isPublicSetter);
            assertFalse(hasPublicSetter, entityType.getSimpleName() + " không được có public setter");
        }
    }

    private void assertEnumString(Class<?> type, String fieldName) throws Exception {
        Enumerated enumerated = field(type, fieldName).getAnnotation(Enumerated.class);
        assertEquals(EnumType.STRING, enumerated.value());
    }

    private void assertLazyWithoutCascade(Class<?> type, String fieldName) throws Exception {
        ManyToOne relationship = field(type, fieldName).getAnnotation(ManyToOne.class);
        assertEquals(FetchType.LAZY, relationship.fetch());
        assertEquals(0, relationship.cascade().length);
        assertFalse(Arrays.asList(relationship.cascade()).contains(CascadeType.PERSIST));
    }

    private boolean isPublicSetter(Method method) {
        return Modifier.isPublic(method.getModifiers())
                && method.getName().startsWith("set")
                && method.getParameterCount() == 1;
    }

    private Field field(Class<?> type, String name) throws NoSuchFieldException {
        return type.getDeclaredField(name);
    }
}
