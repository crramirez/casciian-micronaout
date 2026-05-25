package io.github.crramirez.casciian.micronaut;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.session.ServerSessionAware;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
@Requires(property = "casciian.ssh.enabled", value = "true")
public class CasciianSshServer {
    private final CasciianSshProperties properties;
    private final CasciianSessionHandler sessionHandler;
    private final String hostKeyPath;

    private SshServer sshServer;

    public CasciianSshServer(final CasciianSshProperties properties,
                             final CasciianSessionHandler sessionHandler,
                             @Value("${casciian.ssh.host-key-path:#{null}}") final String hostKeyPath) {
        this.properties = properties;
        this.sessionHandler = sessionHandler;
        this.hostKeyPath = hostKeyPath;
    }

    @PostConstruct
    void start() throws Exception {
        if (!properties.isEnabled()) {
            return;
        }

        sshServer = SshServer.setUpDefaultServer();
        sshServer.setHost(properties.getHost());
        sshServer.setPort(properties.getPort());
        sshServer.setPasswordAuthenticator((username, password, session) ->
                properties.getUsername().equals(username) && properties.getPassword().equals(password));
        sshServer.setShellFactory(channelSession -> new CasciianShellCommand(sessionHandler, properties.getBanner()));

        if (hostKeyPath == null || hostKeyPath.isBlank()) {
            final java.nio.file.Path defaultHostKey = java.nio.file.Path.of(
                    System.getProperty("user.home"), ".casciian", "hostkey.ser");
            java.nio.file.Files.createDirectories(defaultHostKey.getParent());
            sshServer.setKeyPairProvider(new org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider(defaultHostKey));
        } else {
            sshServer.setKeyPairProvider(new org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider(java.nio.file.Path.of(hostKeyPath)));
        }

        sshServer.start();
    }

    @PreDestroy
    void stop() throws Exception {
        if (sshServer != null) {
            sshServer.stop(true);
        }
    }

    static final class CasciianShellCommand implements Command, ServerSessionAware {
        private final CasciianSessionHandler sessionHandler;
        private final String banner;
        private final ExecutorService executor = Executors.newSingleThreadExecutor();

        private InputStream input;
        private OutputStream output;
        private ExitCallback exitCallback;
        private ServerSession session;

        CasciianShellCommand(final CasciianSessionHandler sessionHandler, final String banner) {
            this.sessionHandler = sessionHandler;
            this.banner = banner;
        }

        @Override
        public void setInputStream(final InputStream in) {
            this.input = in;
        }

        @Override
        public void setOutputStream(final OutputStream out) {
            this.output = out;
        }

        @Override
        public void setErrorStream(final OutputStream err) {
            // Shared terminal output stream is enough for this simple shell.
        }

        @Override
        public void setExitCallback(final ExitCallback callback) {
            this.exitCallback = callback;
        }

        @Override
        public void start(final ChannelSession channel, final Environment env) {
            executor.submit(() -> {
                try {
                    output.write((banner + "\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    output.flush();
                    sessionHandler.handle(input, output, Map.of(
                            "transport", "ssh",
                            "username", session != null ? session.getUsername() : "unknown"
                    ));
                    exitCallback.onExit(0);
                } catch (Exception e) {
                    exitCallback.onExit(1, e.getMessage());
                }
            });
        }

        @Override
        public void destroy(final ChannelSession channel) {
            executor.shutdownNow();
        }

        @Override
        public void setSession(final ServerSession session) {
            this.session = session;
        }
    }
}
