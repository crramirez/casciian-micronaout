package io.github.crramirez.casciian.micronaut;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

@FunctionalInterface
public interface CasciianSessionHandler {
    void handle(InputStream input, OutputStream output, Map<String, String> metadata) throws Exception;
}
