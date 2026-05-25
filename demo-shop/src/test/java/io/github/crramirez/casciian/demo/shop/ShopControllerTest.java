package io.github.crramirez.casciian.demo.shop;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShopControllerTest {

    @Test
    void indexRendersHtmlTableAndHeading() {
        final ShopController controller = new ShopController(new ProductRepository());

        final String html = controller.index();

        assertThat(html).contains("Casciian Demo Shop");
        assertThat(html).contains("<table");
    }
}
