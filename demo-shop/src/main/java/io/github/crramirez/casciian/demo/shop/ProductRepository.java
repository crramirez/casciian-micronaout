package io.github.crramirez.casciian.demo.shop;

import jakarta.inject.Singleton;
import net.datafaker.Faker;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class ProductRepository {
    private final Map<Long, Product> products = new LinkedHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public ProductRepository() {
        final Faker faker = new Faker(Locale.ENGLISH);
        for (int i = 0; i < 12; i++) {
            save(faker.commerce().productName(), BigDecimal.valueOf(faker.number().randomDouble(2, 5, 500)));
        }
    }

    public synchronized List<Product> findAll() {
        return products.values().stream()
                .sorted(Comparator.comparing(Product::id))
                .toList();
    }

    public synchronized Product save(final String name, final BigDecimal price) {
        final long id = sequence.incrementAndGet();
        final Product product = new Product(id, name, price);
        products.put(id, product);
        return product;
    }

    public synchronized boolean delete(final long id) {
        return products.remove(id) != null;
    }

    public synchronized List<Product> snapshot() {
        return new ArrayList<>(findAll());
    }
}
