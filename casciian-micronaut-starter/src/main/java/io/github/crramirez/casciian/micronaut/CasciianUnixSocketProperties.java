package io.github.crramirez.casciian.micronaut;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("casciian.unix-socket")
public class CasciianUnixSocketProperties {
    private boolean enabled = true;
    private String path = "/tmp/casciian.sock";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }
}
