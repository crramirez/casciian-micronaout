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

import org.junit.jupiter.api.Test;

import casciian.TApplication;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.exceptions.NoSuchBeanException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * Black-box tests for {@link CasciianUnixSocketConfiguration}.
 */
class CasciianUnixSocketConfigurationTest {

    private static CasciianTApplicationFactory stubFactory() {
        return (in, out, session) -> mock(TApplication.class);
    }

    @Test
    void doesNotLoadByDefault() {
        try (ApplicationContext ctx = ApplicationContext.builder()
                .singletons(stubFactory()).build().start()) {
            assertThatThrownBy(() -> ctx.getBean(CasciianUnixSocketServer.class))
                    .isInstanceOf(NoSuchBeanException.class);
        }
    }

    @Test
    void doesNotLoadWhenExplicitlyDisabled() {
        final Map<String, Object> props = Map.of(
                "casciian.unix-socket.enabled", "false");
        try (ApplicationContext ctx = ApplicationContext.builder(props)
                .singletons(stubFactory()).build().start()) {
            assertThatThrownBy(() -> ctx.getBean(CasciianUnixSocketServer.class))
                    .isInstanceOf(NoSuchBeanException.class);
        }
    }

    @Test
    void loadsAndBindsPropertiesWhenEnabled() {
        final Map<String, Object> props = Map.of(
                "casciian.unix-socket.enabled", "true",
                "casciian.unix-socket.auto-start", "false",
                "casciian.unix-socket.path", "/tmp/test.sock",
                "casciian.unix-socket.permissions", "600");
        try (ApplicationContext ctx = ApplicationContext.builder(props)
                .singletons(stubFactory()).build().start()) {
            final CasciianUnixSocketServer server = ctx.getBean(CasciianUnixSocketServer.class);
            assertThat(server).isNotNull();
            final CasciianUnixSocketProperties bound =
                    ctx.getBean(CasciianUnixSocketProperties.class);
            assertThat(bound.isEnabled()).isTrue();
            assertThat(bound.getPath()).isEqualTo("/tmp/test.sock");
            assertThat(bound.getPermissions()).isEqualTo("600");
        }
    }

    @Test
    void usesDefaultsWhenOnlyEnabledFlagIsSet() {
        final Map<String, Object> props = Map.of(
                "casciian.unix-socket.enabled", "true",
                "casciian.unix-socket.auto-start", "false");
        try (ApplicationContext ctx = ApplicationContext.builder(props)
                .singletons(stubFactory()).build().start()) {
            final CasciianUnixSocketProperties bound =
                    ctx.getBean(CasciianUnixSocketProperties.class);
            assertThat(bound.getPath()).isEqualTo(CasciianUnixSocketProperties.DEFAULT_PATH);
            assertThat(bound.getPermissions())
                    .isEqualTo(CasciianUnixSocketProperties.DEFAULT_PERMISSIONS);
        }
    }
}
