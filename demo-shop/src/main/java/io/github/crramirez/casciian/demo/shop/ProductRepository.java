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

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

/**
 * Micronaut Data repository for {@link Product}. The web layer uses
 * {@link #findAll()} for the customer-facing catalogue, and the admin TUI
 * uses the full CRUD surface inherited from {@link CrudRepository}.
 *
 * <p>Backed by the H2 dialect; Micronaut Data generates the SQL at compile
 * time, which keeps the GraalVM native image free of runtime
 * reflection.</p>
 */
@JdbcRepository(dialect = Dialect.H2)
public interface ProductRepository extends CrudRepository<Product, Long> {
}
