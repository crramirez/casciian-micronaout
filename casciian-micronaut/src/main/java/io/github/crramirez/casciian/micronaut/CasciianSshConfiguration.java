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

import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.RejectAllPasswordAuthenticator;
import org.apache.sshd.server.shell.ShellFactory;

import casciian.TApplication;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

/**
 * Micronaut {@link Factory} that wires an embedded SSH server serving a
 * Casciian {@link TApplication} per connection.
 *
 * <p>The factory is active when:</p>
 * <ul>
 *   <li>{@link TApplication} is on the classpath (the integration depends
 *       on the Casciian library, so this is effectively always true but
 *       keeps the guard explicit).</li>
 *   <li>Property {@code casciian.ssh.enabled} is not set to {@code false}.
 *       Note the {@code defaultValue = "true"} on the {@link Requires}
 *       annotation &mdash; this matches Spring's
 *       {@code matchIfMissing = true} semantics so adding the integration
 *       to an empty configuration just works.</li>
 * </ul>
 *
 * <p>Applications <strong>must</strong> publish a
 * {@link CasciianTApplicationFactory} bean; bean creation will fail at
 * startup if none is present, because there is no sensible default TUI.</p>
 */
@Factory
@Requires(classes = TApplication.class)
@Requires(property = "casciian.ssh.enabled", value = "true", defaultValue = "true")
public class CasciianSshConfiguration {

    /**
     * Default password authenticator backed by
     * {@link CasciianSshProperties#getUsername()} /
     * {@link CasciianSshProperties#getPassword()}.
     *
     * <p>If either is blank the returned authenticator rejects every attempt,
     * so a misconfigured integration never silently grants anonymous
     * access. Applications can override this by declaring their own
     * {@link PasswordAuthenticator} bean (e.g. backed by Micronaut
     * Security).</p>
     */
    @Bean
    @Singleton
    @Requires(missingBeans = PasswordAuthenticator.class)
    public PasswordAuthenticator casciianSshPasswordAuthenticator(
            final CasciianSshProperties properties) {
        final String expectedUser = properties.getUsername();
        final String expectedPassword = properties.getPassword();
        if (expectedUser == null || expectedUser.isBlank()
                || expectedPassword == null || expectedPassword.isBlank()) {
            return RejectAllPasswordAuthenticator.INSTANCE;
        }
        return (username, password, session) ->
                expectedUser.equals(username) && expectedPassword.equals(password);
    }

    @Bean
    @Singleton
    @Requires(missingBeans = ShellFactory.class)
    public ShellFactory casciianShellFactory(final CasciianTApplicationFactory factory) {
        return new CasciianShellFactory(factory);
    }

    @Bean(preDestroy = "stop")
    @Singleton
    @Requires(missingBeans = CasciianSshServer.class)
    public CasciianSshServer casciianSshServer(final CasciianSshProperties properties,
                                               final ShellFactory shellFactory,
                                               final PasswordAuthenticator authenticator) {
        // Start/stop is driven by the StartupEvent listener implemented by
        // CasciianSshServer and a @PreDestroy hook bound to the bean's
        // stop() method.
        return new CasciianSshServer(properties, shellFactory, authenticator);
    }
}
