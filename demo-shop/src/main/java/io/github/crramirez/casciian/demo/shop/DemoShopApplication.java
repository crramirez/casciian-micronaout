package io.github.crramirez.casciian.demo.shop;

import io.github.crramirez.casciian.micronaut.client.CasciianConsoleClient;
import io.micronaut.runtime.Micronaut;

import java.nio.file.Path;

public class DemoShopApplication {

    public static void main(final String[] args) throws Exception {
        if (args.length > 0 && "console".equalsIgnoreCase(args[0])) {
            final String configured = System.getProperty("casciian.unix-socket.path", "/tmp/casciian.sock");
            CasciianConsoleClient.run(Path.of(configured));
            return;
        }

        Micronaut.run(DemoShopApplication.class, args);
    }
}
