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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.datafaker.Faker;

/**
 * Populates the H2 product table with random data on startup, but only when
 * the table is empty so the seed survives application restarts in profiles
 * that use a persisted store.
 *
 * <p>Implemented as a separate, package-private bean so the logic can be
 * unit-tested directly against an in-memory list without standing up the
 * full Micronaut context.</p>
 *
 * <p>Seeding is triggered by Micronaut's {@link StartupEvent}, which is the
 * equivalent of Spring's {@code ApplicationReadyEvent}: it fires once the
 * application context has finished refreshing and is ready to serve
 * traffic.</p>
 */
@Singleton
@Requires(notEnv = Environment.TEST)
class ProductFakerSeeder implements ApplicationEventListener<StartupEvent> {

    /**
     * Number of products created on first startup.
     */
    static final int DEFAULT_PRODUCT_COUNT = 25;

    private final ProductRepository repository;
    private final Faker faker;
    private final Random random;
    private final int productCount;

    @Inject
    ProductFakerSeeder(final ProductRepository repository) {
        // Use a fixed seed so the seeded catalogue is reproducible across
        // restarts, which makes the demo screenshots stable. {@link
        // Locale#ENGLISH} keeps the generated words easy to read for the
        // typical demo audience.
        this(repository,
                new Faker(Locale.ENGLISH, new Random(42L)),
                new Random(42L),
                DEFAULT_PRODUCT_COUNT);
    }

    // Visible for testing.
    ProductFakerSeeder(final ProductRepository repository,
                       final Faker faker,
                       final Random random,
                       final int productCount) {
        this.repository = repository;
        this.faker = faker;
        this.random = random;
        this.productCount = productCount;
    }

    @Override
    public void onApplicationEvent(final StartupEvent event) {
        seed();
    }

    /**
     * Public entry-point exposed for tests. Returns the products that were
     * actually created (empty if the table was already populated).
     *
     * @return the freshly seeded products
     */
    public List<Product> seed() {
        if (repository.count() > 0) {
            return List.of();
        }
        final List<Product> created = new ArrayList<>(productCount);
        for (int i = 0; i < productCount; i++) {
            created.add(repository.save(generate(i)));
        }
        return created;
    }

    /**
     * Build a single random product. Public for unit tests so we can verify
     * generation without saving anything.
     *
     * @param index 0-based index used to make the SKU unique even when
     *              Faker happens to produce the same commerce code twice
     * @return a transient {@link Product} ready to be persisted
     */
    Product generate(final int index) {
        final String name = capitalize(faker.commerce().productName());
        final String description = faker.lorem().sentence(12);
        // Generate prices locally so precision and locale stay under our control.
        final BigDecimal price = BigDecimal.valueOf(0.5 + random.nextDouble() * 199.5)
                .setScale(2, RoundingMode.HALF_UP);
        final int stock = random.nextInt(200);
        // Faker's promotionCode is unique-enough across a small batch but we
        // suffix the index defensively so SKU uniqueness is guaranteed.
        final String sku = (faker.commerce().promotionCode() + "-" + index).toUpperCase(Locale.ROOT);
        return new Product(sku, name, description, price, stock);
    }

    private static String capitalize(final String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
