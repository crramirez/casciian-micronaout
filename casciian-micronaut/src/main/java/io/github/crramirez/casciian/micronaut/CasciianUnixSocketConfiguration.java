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

import casciian.TApplication;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

/**
 * Micronaut {@link Factory} that wires the Unix-domain-socket variant
 * of the Casciian listener.
 *
 * <p>The configuration is independent of {@link CasciianSshConfiguration}
 * so the two listeners can be enabled together, separately, or both
 * disabled. Both rely on the same user-supplied
 * {@link CasciianTApplicationFactory} bean &mdash; there is one TUI
 * definition, exposed through whichever transport(s) the operator picks.</p>
 *
 * <p>Active when:</p>
 * <ul>
 *   <li>{@link TApplication} is on the classpath.</li>
 *   <li>Property {@code casciian.unix-socket.enabled=true} is set
 *       explicitly. The default is {@code false} because the listener
 *       creates a file inside the running container and operators should
 *       opt in.</li>
 * </ul>
 */
@Factory
@Requires(classes = TApplication.class)
@Requires(property = "casciian.unix-socket.enabled", value = "true")
public class CasciianUnixSocketConfiguration {

    @Bean(preDestroy = "stop")
    @Singleton
    @Requires(missingBeans = CasciianUnixSocketServer.class)
    public CasciianUnixSocketServer casciianUnixSocketServer(
            final CasciianUnixSocketProperties properties,
            final CasciianTApplicationFactory applicationFactory) {
        return new CasciianUnixSocketServer(properties, applicationFactory);
    }
}
