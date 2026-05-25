package io.github.crramirez.casciian.demo.shop;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/")
public class ShopController {
    private final ProductRepository repository;

    public ShopController(final ProductRepository repository) {
        this.repository = repository;
    }

    @Get(produces = MediaType.TEXT_HTML)
    public String index() {
        final StringBuilder html = new StringBuilder();
        html.append("<!doctype html><html><head><meta charset=\"utf-8\"><title>Casciian Demo Shop</title></head><body>");
        html.append("<h1>Casciian Demo Shop</h1>");
        html.append("<p>Micronaut customer view over the same product data used by the admin terminal.</p>");
        html.append("<table border=\"1\" cellpadding=\"8\" cellspacing=\"0\"><thead><tr><th>ID</th><th>Name</th><th>Price</th></tr></thead><tbody>");
        for (Product product : repository.findAll()) {
            html.append("<tr><td>").append(product.id()).append("</td><td>")
                    .append(escape(product.name())).append("</td><td>$")
                    .append(product.price()).append("</td></tr>");
        }
        html.append("</tbody></table>");
        html.append("</body></html>");
        return html.toString();
    }

    private static String escape(final String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
