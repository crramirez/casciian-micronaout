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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.views.View;

/**
 * Customer-facing storefront. Renders the catalogue from the H2 database
 * using the same {@link ProductRepository} the admin TUI mutates, so changes
 * made through the TUI show up here on the next page refresh.
 */
@Controller("/")
public class ShopController {

    private final ProductRepository products;

    public ShopController(final ProductRepository products) {
        this.products = products;
    }

    @Get
    @View("catalogue")
    @Produces(MediaType.TEXT_HTML)
    public Map<String, Object> catalogue() {
        final List<Product> sorted = new ArrayList<>();
        products.findAll().forEach(sorted::add);
        sorted.sort(Comparator.comparing(
                p -> p.getName() == null ? "" : p.getName(),
                String.CASE_INSENSITIVE_ORDER));
        return Map.of("products", sorted);
    }
}
