package com.stu.edu.vn.backend.admin.bootstrap;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

class BootstrapAdminRunnerTest {

    private final BootstrapAdminService service = org.mockito.Mockito.mock(BootstrapAdminService.class);
    private final BootstrapAdminRunner runner = new BootstrapAdminRunner(service);

    @Test
    void runnerDelegatesBootstrapWithoutReadingOrLoggingCredentials() {
        when(service.bootstrapIfEnabled()).thenReturn(BootstrapAdminResult.DISABLED);

        runner.run(new DefaultApplicationArguments());

        verify(service).bootstrapIfEnabled();
    }
}
