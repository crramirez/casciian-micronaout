/*
 * Copyright 2026 Carlos Rafael Ramirez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.crramirez.casciian.micronaut;

import java.util.Map;

import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.RejectAllPasswordAuthenticator;
import org.apache.sshd.server.shell.ShellFactory;
import org.junit.jupiter.api.Test;

import casciian.TApplication;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.exceptions.NoSuchBeanException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * Black-box tests for {@link CasciianSshConfiguration}.
 *
 * <p>These verify the public contract of the integration (which beans are
 * created, when it stays silent, what overriding produces) without
 * exercising the real SSH network listener. The integration's
 * auto-bind-on-startup behavior is suppressed via {@code
 * casciian.ssh.auto-start=false}; the bean wiring is exactly the same.</p>
 */
class CasciianSshConfigurationTest {

    /** Properties that disable the actual network bind during tests. */
    private static final Map<String, Object> NO_AUTOSTART = Map.of(
            "casciian.ssh.auto-start", "false");

    /**
     * Stub TApplication factory used in tests. We register it as a manual
     * singleton through {@link ApplicationContext.Builder#singletons(Object...)}
     * rather than as a discovered Micronaut bean, so individual tests can
     * choose whether the integration's required collaborator is present.
     */
    private static CasciianTApplicationFactory stubFactory() {
        return (in, out, session) -> mock(TApplication.class);
    }

    @Test
    void failsFastWithoutATApplicationFactory() {
        // CasciianSshServer is an ApplicationEventListener<StartupEvent>, so
        // its dependencies are resolved eagerly when the context starts.
        // With no CasciianTApplicationFactory bean available, start() fails.
        assertThatThrownBy(() -> {
            try (ApplicationContext ctx = ApplicationContext.run(NO_AUTOSTART)) {
                // never reached
                ctx.getBean(CasciianSshServer.class);
            }
        }).hasMessageContaining(CasciianTApplicationFactory.class.getSimpleName());
    }

    @Test
    void doesNotLoadWhenDisabled() {
        final Map<String, Object> disabled = Map.of(
                "casciian.ssh.enabled", "false",
                "casciian.ssh.auto-start", "false");
        try (ApplicationContext ctx = ApplicationContext.builder(disabled)
                .singletons(stubFactory()).build().start()) {
            // The @Factory itself is gated on casciian.ssh.enabled, so none of
            // the beans it declares (ShellFactory, PasswordAuthenticator,
            // CasciianSshServer) are loaded.
            assertThatThrownBy(() -> ctx.getBean(CasciianSshServer.class))
                    .isInstanceOf(NoSuchBeanException.class);
        }
    }

    @Test
    void bindsDefaultPropertiesWhenEnabled() {
        try (ApplicationContext ctx = ApplicationContext.builder(NO_AUTOSTART)
                .singletons(stubFactory()).build().start()) {
            final CasciianSshProperties props = ctx.getBean(CasciianSshProperties.class);
            assertThat(props.isEnabled()).isTrue();
            assertThat(props.getPort()).isEqualTo(CasciianSshProperties.DEFAULT_PORT);
            assertThat(props.getHost()).isEqualTo(CasciianSshProperties.DEFAULT_HOST);
            assertThat(props.getHostKeyPath())
                    .isEqualTo(CasciianSshProperties.DEFAULT_HOST_KEY_PATH);
        }
    }

    @Test
    void bindsOverriddenProperties() {
        final Map<String, Object> overrides = Map.of(
                "casciian.ssh.auto-start", "false",
                "casciian.ssh.port", "3333",
                "casciian.ssh.host", "127.0.0.1",
                "casciian.ssh.username", "admin",
                "casciian.ssh.password", "hunter2",
                "casciian.ssh.host-key-path", "/tmp/key",
                "casciian.ssh.banner", "Hello");
        try (ApplicationContext ctx = ApplicationContext.builder(overrides)
                .singletons(stubFactory()).build().start()) {
            final CasciianSshProperties props = ctx.getBean(CasciianSshProperties.class);
            assertThat(props.getPort()).isEqualTo(3333);
            assertThat(props.getHost()).isEqualTo("127.0.0.1");
            assertThat(props.getUsername()).isEqualTo("admin");
            assertThat(props.getPassword()).isEqualTo("hunter2");
            assertThat(props.getHostKeyPath()).isEqualTo("/tmp/key");
            assertThat(props.getBanner()).isEqualTo("Hello");
        }
    }

    @Test
    void defaultAuthenticatorRejectsEverythingWhenCredentialsMissing() {
        try (ApplicationContext ctx = ApplicationContext.builder(NO_AUTOSTART)
                .singletons(stubFactory()).build().start()) {
            final PasswordAuthenticator auth = ctx.getBean(PasswordAuthenticator.class);
            assertThat(auth).isSameAs(RejectAllPasswordAuthenticator.INSTANCE);
        }
    }

    @Test
    void defaultAuthenticatorAcceptsOnlyConfiguredCredentials() {
        final Map<String, Object> creds = Map.of(
                "casciian.ssh.auto-start", "false",
                "casciian.ssh.username", "admin",
                "casciian.ssh.password", "hunter2");
        try (ApplicationContext ctx = ApplicationContext.builder(creds)
                .singletons(stubFactory()).build().start()) {
            final PasswordAuthenticator auth = ctx.getBean(PasswordAuthenticator.class);
            assertThat(auth.authenticate("admin", "hunter2", null)).isTrue();
            assertThat(auth.authenticate("admin", "wrong", null)).isFalse();
            assertThat(auth.authenticate("root", "hunter2", null)).isFalse();
        }
    }

    @Test
    void userProvidedBeansOverrideDefaults() {
        final PasswordAuthenticator userAuth = mock(PasswordAuthenticator.class);
        final ShellFactory userShell = mock(ShellFactory.class);
        try (ApplicationContext ctx = ApplicationContext.builder(NO_AUTOSTART)
                .singletons(stubFactory(), userAuth, userShell).build().start()) {
            assertThat(ctx.getBean(PasswordAuthenticator.class)).isSameAs(userAuth);
            assertThat(ctx.getBean(ShellFactory.class)).isSameAs(userShell);
            // The server is still created using whatever the user supplied.
            assertThat(ctx.getBean(CasciianSshServer.class)).isNotNull();
        }
    }
}
