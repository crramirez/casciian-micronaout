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

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;

/**
 * A product in the demo shop catalogue.
 *
 * <p>Mutable Micronaut Data entity, on purpose: the admin TUI updates
 * fields in place before saving back through the repository.</p>
 *
 * <p>Mapped via Micronaut Data's compile-time JDBC support (no Hibernate,
 * no runtime reflection) which keeps the GraalVM native build small.</p>
 */
@MappedEntity("products")
public class Product {

    @Id
    @GeneratedValue(GeneratedValue.Type.IDENTITY)
    private Long id;

    private String sku;
    private String name;
    private String description;
    // Schema-generate emits NUMERIC without a scale by default which makes H2
    // round to integer. Pin DECIMAL(10,2) so prices keep their two-decimal
    // precision end-to-end.
    @MappedProperty(definition = "DECIMAL(10,2)")
    private BigDecimal price;
    private int stock;

    /**
     * No-args constructor used by Micronaut Data when materializing rows.
     */
    public Product() {
    }

    /**
     * Convenience constructor used by tests and the seeder.
     *
     * @param sku         unique stock-keeping unit
     * @param name        product display name
     * @param description marketing copy (may be null)
     * @param price       unit price; must be non-null
     * @param stock       on-hand quantity
     */
    public Product(final String sku, final String name, final String description,
                   final BigDecimal price, final int stock) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(final String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(final BigDecimal price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(final int stock) {
        this.stock = stock;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Product other)) {
            return false;
        }
        // Transient entities (id == null) are only equal to themselves to
        // avoid collapsing distinct unsaved products into one Set/Map entry.
        if (id == null || other.id == null) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        // Use a constant for transient entities so that adding an unsaved
        // product to a collection and then persisting it (which mutates id)
        // does not corrupt hash-based lookups.
        return id == null ? 0 : id.hashCode();
    }

    @Override
    public String toString() {
        return "Product{id=" + id + ", sku='" + sku + "', name='" + name
                + "', price=" + price + ", stock=" + stock + '}';
    }
}
