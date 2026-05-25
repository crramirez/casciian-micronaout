/*
 * Copyright 2026 Carlos Rafael Ramirez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.crramirez.casciian.demo.shop;

import io.github.crramirez.casciian.micronaut.client.CasciianConsoleClient;
import io.micronaut.runtime.Micronaut;

/**
 * Entry point for the demo shop. The application exposes:
 *
 * <ul>
 *   <li>A customer-facing product catalogue at <code>http://localhost:8080/</code>
 *       backed by an in-memory H2 database seeded with random products.</li>
 *   <li>A Casciian admin TUI reachable over SSH at port 2222 that allows
 *       full CRUD on the same product catalogue.</li>
 *   <li>The same admin TUI reachable over a Unix-domain socket at
 *       {@code /tmp/casciian.sock} so operators inside the container
 *       (reached via {@code docker exec}, {@code kubectl exec}, or an
 *       ArgoCD terminal) can attach without needing SSH.</li>
 * </ul>
 *
 * <p>Passing the literal argument {@value CasciianConsoleClient#CONSOLE_ARGUMENT}
 * to {@code main} switches the JAR (or native executable) into
 * <em>console-client mode</em>: it does not boot Micronaut at all, only
 * connects to the local Unix socket and acts as a thin terminal
 * multiplexer between the user's TTY and the already-running server
 * process. This is what lets a single GraalVM native binary serve as both
 * the server and the operator's terminal client.</p>
 *
 * <p>The whole point of this demo is to show that a single Micronaut
 * application &mdash; deployable as a native executable thanks to
 * Micronaut's compile-time DI &mdash; can serve both audiences from one
 * process, sharing the same beans (repositories, services).</p>
 */
public final class DemoShopApplication {

    private DemoShopApplication() {
        // Entry point only.
    }

    /**
     * Standard Micronaut main method.
     *
     * <p>When invoked with {@code "console"} as one of the program
     * arguments, the method delegates to {@link CasciianConsoleClient}
     * and returns without starting the Micronaut context. This is the
     * "attach to the in-container TUI over IPC" code path.</p>
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        if (CasciianConsoleClient.isConsoleInvocation(args)) {
            final int exitCode = new CasciianConsoleClient().run();
            if (exitCode != 0) {
                System.exit(exitCode);
            }
            return;
        }
        Micronaut.run(DemoShopApplication.class, args);
    }
}
