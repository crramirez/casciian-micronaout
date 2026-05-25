package io.github.crramirez.casciian.demo.shop.admin;

import io.github.crramirez.casciian.demo.shop.ProductRepository;
import io.github.crramirez.casciian.micronaut.CasciianSessionHandler;
import jakarta.inject.Singleton;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Singleton
public class AdminSessionHandler implements CasciianSessionHandler {
    private final ProductRepository repository;

    public AdminSessionHandler(final ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public void handle(final InputStream input, final OutputStream output, final Map<String, String> metadata) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(output, true, StandardCharsets.UTF_8)) {

            writer.println("Connected via " + metadata.getOrDefault("transport", "unknown") + ". Commands: list | new <name> <price> | delete <id> | help | quit");

            String line;
            while ((line = reader.readLine()) != null) {
                final String trimmed = line.trim();
                if (trimmed.isEmpty() || "help".equalsIgnoreCase(trimmed)) {
                    writer.println("Commands: list | new <name> <price> | delete <id> | quit");
                    continue;
                }
                if ("quit".equalsIgnoreCase(trimmed) || "exit".equalsIgnoreCase(trimmed)) {
                    writer.println("Bye");
                    return;
                }
                if ("list".equalsIgnoreCase(trimmed)) {
                    repository.findAll().forEach(p -> writer.printf("%d | %s | %s%n", p.id(), p.name(), p.price()));
                    continue;
                }
                if (trimmed.startsWith("new ")) {
                    final String[] split = trimmed.substring(4).trim().split(" ");
                    if (split.length < 2) {
                        writer.println("Usage: new <name> <price>");
                        continue;
                    }
                    final String name = split[0];
                    final BigDecimal price = new BigDecimal(split[1]);
                    final var created = repository.save(name, price);
                    writer.println("Created product " + created.id());
                    continue;
                }
                if (trimmed.startsWith("delete ")) {
                    final long id = Long.parseLong(trimmed.substring(7).trim());
                    writer.println(repository.delete(id) ? "Deleted" : "Not found");
                    continue;
                }

                writer.println("Unknown command. Type help.");
            }
        }
    }
}
