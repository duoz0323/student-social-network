package com.stu.edu.vn.backend.moderation.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.stu.edu.vn.backend.moderation.config.ContentModerationProperties;
import com.stu.edu.vn.backend.moderation.provider.ModerationProvider;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import tools.jackson.databind.ObjectMapper;

class LocalAiModerationProviderSpringTest {

    @Test
    void springCreatesOnlyTheLocalProviderWithoutAnyPaidProviderConfiguration() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(ContentModerationProperties.class);
            context.registerBean(ObjectMapper.class);
            context.register(LocalAiModerationProvider.class);
            context.refresh();

            assertThat(context.getBeansOfType(ModerationProvider.class))
                    .hasSize(1)
                    .allSatisfy((name, provider) -> assertThat(provider)
                            .isInstanceOf(LocalAiModerationProvider.class));
        }
    }
}
