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

import java.math.BigDecimal;
import java.util.List;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end test of the customer-facing shop. We boot the full Micronaut
 * context (H2 + Micronaut Data JDBC + Netty + Thymeleaf views) and verify
 * the catalogue page renders the products that the {@link ProductRepository}
 * actually contains. Casciian's SSH server is disabled so the test does
 * not bind to port 2222, and the Unix-socket listener is disabled so the
 * test does not touch the file system.
 */
@MicronautTest(propertySources = "classpath:application-test.yml", transactional = false)
class ShopControllerIntegrationTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    ProductRepository repository;

    @Test
    void cataloguePageListsAllProductsFromTheDatabase() {
        repository.deleteAll();
        repository.saveAll(List.of(
                new Product("SKU-A", "Apple", "Red and crisp", new BigDecimal("1.50"), 10),
                new Product("SKU-B", "Banana", "Yellow and ripe", new BigDecimal("0.40"), 20)));

        final HttpResponse<String> response = client.toBlocking().exchange(
                HttpRequest.GET("/").accept(MediaType.TEXT_HTML),
                String.class);

        assertThat(response.getStatus().getCode()).isEqualTo(200);
        final String body = response.body();
        assertThat(body)
                .contains("Apple")
                .contains("Banana")
                .contains("$1.50");
    }
}
