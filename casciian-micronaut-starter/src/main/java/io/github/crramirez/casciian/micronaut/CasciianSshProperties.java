package io.github.crramirez.casciian.micronaut;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("casciian.ssh")
public class CasciianSshProperties {
    private boolean enabled = true;
    private String host = "127.0.0.1";
    private int port = 2222;
    private String username = "admin";
    private String password = "admin";
    private String banner = "Welcome to the Casciian Micronaut admin console";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(final String banner) {
        this.banner = banner;
    }
}
