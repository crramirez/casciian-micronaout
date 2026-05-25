package io.github.crramirez.casciian.micronaut;

import io.micronaut.context.annotation.Requires;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;

import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
@Requires(property = "casciian.unix-socket.enabled", value = "true")
public class CasciianUnixSocketServer {
    private final CasciianUnixSocketProperties properties;
    private final CasciianSessionHandler sessionHandler;
    private final ExecutorService acceptor = Executors.newSingleThreadExecutor();

    private ServerSocketChannel serverSocket;

    public CasciianUnixSocketServer(final CasciianUnixSocketProperties properties,
                                    final CasciianSessionHandler sessionHandler) {
        this.properties = properties;
        this.sessionHandler = sessionHandler;
    }

    @PostConstruct
    void start() throws Exception {
        if (!properties.isEnabled()) {
            return;
        }

        final Path socketPath = Path.of(properties.getPath()).toAbsolutePath();
        Files.createDirectories(socketPath.getParent());
        Files.deleteIfExists(socketPath);

        serverSocket = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
        serverSocket.bind(UnixDomainSocketAddress.of(socketPath));

        acceptor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try (SocketChannel channel = serverSocket.accept()) {
                    sessionHandler.handle(
                            Channels.newInputStream(channel),
                            Channels.newOutputStream(channel),
                            Map.of("transport", "unix-socket", "path", socketPath.toString())
                    );
                } catch (Exception ignored) {
                    if (serverSocket == null || !serverSocket.isOpen()) {
                        break;
                    }
                }
            }
            return null;
        });
    }

    @PreDestroy
    void stop() throws Exception {
        if (serverSocket != null) {
            serverSocket.close();
        }
        acceptor.shutdownNow();
    }
}
