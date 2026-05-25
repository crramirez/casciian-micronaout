package io.github.crramirez.casciian.demo.shop;

import java.math.BigDecimal;

public record Product(long id, String name, BigDecimal price) {
}
