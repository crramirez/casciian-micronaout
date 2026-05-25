package io.github.crramirez.casciian.demo.shop;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRepositoryTest {

    @Test
    void saveAddsANewProduct() {
        final ProductRepository repository = new ProductRepository();
        final int initial = repository.findAll().size();

        final Product created = repository.save("manual-test-item", BigDecimal.valueOf(9.99));

        assertThat(repository.findAll()).hasSize(initial + 1);
        assertThat(repository.findAll()).contains(created);
    }
}
