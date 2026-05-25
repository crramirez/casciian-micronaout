package io.github.crramirez.casciian.micronaut;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CasciianPropertiesTest {

    @Test
    void sshPropertiesHaveDemoFriendlyDefaults() {
        final CasciianSshProperties props = new CasciianSshProperties();

        assertThat(props.isEnabled()).isTrue();
        assertThat(props.getHost()).isEqualTo("127.0.0.1");
        assertThat(props.getPort()).isEqualTo(2222);
        assertThat(props.getUsername()).isEqualTo("admin");
        assertThat(props.getPassword()).isEqualTo("admin");
    }

    @Test
    void unixSocketPropertiesPointToTmpByDefault() {
        final CasciianUnixSocketProperties props = new CasciianUnixSocketProperties();

        assertThat(props.isEnabled()).isTrue();
        assertThat(props.getPath()).isEqualTo("/tmp/casciian.sock");
    }
}
