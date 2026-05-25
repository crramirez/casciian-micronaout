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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.ShutdownEvent;
import io.micronaut.context.event.StartupEvent;
import jakarta.annotation.PreDestroy;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ShellFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lifecycle-aware bean that owns the embedded MINA
 * {@link SshServer}. It is started when Micronaut publishes
 * {@link StartupEvent} (after the rest of the application context has
 * finished bootstrapping) and stopped on {@link ShutdownEvent} (or via
 * {@link PreDestroy} as a safety net).
 *
 * <p>In Micronaut there is no equivalent of Spring's {@code SmartLifecycle}
 * phase ordering &mdash; bean creation and event delivery happen in
 * dependency order &mdash; but listening to {@code StartupEvent} reliably
 * delays {@code start()} until the rest of the context (notably the
 * Micronaut HTTP server, which is itself a {@code StartupEvent} listener)
 * has been wired.</p>
 */
public class CasciianSshServer
        implements ApplicationEventListener<StartupEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(CasciianSshServer.class);

    private final CasciianSshProperties properties;
    private final ShellFactory shellFactory;
    private final PasswordAuthenticator passwordAuthenticator;

    private final AtomicReference<SshServer> sshServer = new AtomicReference<>();
    private volatile boolean running;

    public CasciianSshServer(final CasciianSshProperties properties,
                             final ShellFactory shellFactory,
                             final PasswordAuthenticator passwordAuthenticator) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null");
        }
        // shellFactory and passwordAuthenticator may legitimately be null
        // when the bean is constructed by hand for the path-resolution unit
        // tests. Dependency injection guarantees they are non-null in
        // production, where they are required before start() runs.
        this.properties = properties;
        this.shellFactory = shellFactory;
        this.passwordAuthenticator = passwordAuthenticator;
    }

    @Override
    public void onApplicationEvent(final StartupEvent event) {
        if (!properties.isAutoStart()) {
            // Allow tests (and applications with bespoke lifecycles) to opt
            // out of the automatic bind without losing the rest of the
            // bean wiring.
            LOG.debug("Casciian SSH server auto-start disabled; call start() manually");
            return;
        }
        start();
    }

    /**
     * Shutdown hook backing the Micronaut {@link ShutdownEvent}. Declared
     * separately so the same logic can be invoked from {@link PreDestroy}
     * and from unit tests.
     */
    public synchronized void start() {
        if (running) {
            return;
        }
        final SshServer server = SshServer.setUpDefaultServer();
        server.setHost(properties.getHost());
        server.setPort(properties.getPort());
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(resolveHostKeyPath()));
        server.setShellFactory(shellFactory);
        server.setPasswordAuthenticator(passwordAuthenticator);
        final String banner = properties.getBanner();
        if (banner != null && !banner.isEmpty()) {
            // Welcome banner sent during user-auth (RFC 4252 §5.4). Stored via
            // the generic property map so this module does not pin a specific
            // MINA SSHD constant name across versions.
            server.getProperties().put("welcome-banner", banner);
        }
        try {
            server.start();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to start Casciian SSH server on "
                            + properties.getHost() + ":" + properties.getPort(), e);
        }
        sshServer.set(server);
        running = true;
        LOG.info("Casciian SSH server listening on {}:{}",
                properties.getHost(), properties.getPort());
    }

    @PreDestroy
    public synchronized void stop() {
        if (!running) {
            return;
        }
        final SshServer server = sshServer.get();
        if (server != null) {
            try {
                server.stop(true);
            } catch (IOException e) {
                LOG.warn("Error stopping Casciian SSH server", e);
            }
        }
        sshServer.set(null);
        running = false;
        LOG.info("Casciian SSH server stopped");
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * Resolve the configured host-key path, expanding a leading {@code ~} to
     * the user's home directory and ensuring parent directories exist.
     */
    Path resolveHostKeyPath() {
        final String configured = properties.getHostKeyPath();
        final String raw = (configured == null || configured.isBlank())
                ? CasciianSshProperties.DEFAULT_HOST_KEY_PATH
                : configured;
        final String expanded;
        if (raw.startsWith("~/")) {
            expanded = System.getProperty("user.home")
                    + raw.substring(1);
        } else if (raw.equals("~")) {
            throw new IllegalStateException(
                    "Invalid host-key path '~': expected a file path such as '~/hostkey.ser'");
        } else {
            expanded = raw;
        }
        final Path path = Paths.get(expanded).toAbsolutePath();
        final Path parent = path.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Unable to create host-key directory " + parent, e);
            }
        }
        return path;
    }
}
