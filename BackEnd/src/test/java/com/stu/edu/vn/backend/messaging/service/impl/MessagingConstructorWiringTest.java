package com.stu.edu.vn.backend.messaging.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/** Chống tái phát lỗi Spring không chọn được constructor của các service Messaging. */
class MessagingConstructorWiringTest {

    @Test
    void messagingServicesExposeOneUnambiguousDependencyInjectionConstructor() {
        assertSpringCanCreate(MessageAttachmentAccessServiceImpl.class);
        assertSpringCanCreate(MessagingImageServiceImpl.class);
        assertSpringCanCreate(MessagingServiceImpl.class);
    }

    private void assertSpringCanCreate(Class<?> serviceType) {
        Constructor<?>[] constructors = serviceType.getDeclaredConstructors();
        assertThat(constructors).hasSize(1);

        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        for (Class<?> dependencyType : constructors[0].getParameterTypes()) {
            factory.registerResolvableDependency(dependencyType, mock(dependencyType));
        }

        assertThat(factory.createBean(serviceType)).isInstanceOf(serviceType);
    }
}
