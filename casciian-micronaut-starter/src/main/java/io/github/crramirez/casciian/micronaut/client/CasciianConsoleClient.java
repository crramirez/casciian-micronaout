package io.github.crramirez.casciian.micronaut.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;

public final class CasciianConsoleClient {
    private CasciianConsoleClient() {
    }

    public static void run(final Path socketPath) throws Exception {
        try (SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX)) {
            channel.connect(UnixDomainSocketAddress.of(socketPath.toAbsolutePath()));

            final InputStream serverInput = Channels.newInputStream(channel);
            final OutputStream serverOutput = Channels.newOutputStream(channel);

            final Thread inbound = new Thread(() -> pipe(serverInput, System.out), "casciian-console-client-inbound");
            final Thread outbound = new Thread(() -> pipe(System.in, serverOutput), "casciian-console-client-outbound");
            inbound.setDaemon(true);
            outbound.setDaemon(true);

            inbound.start();
            outbound.start();

            outbound.join();
        }
    }

    private static void pipe(final InputStream input, final OutputStream output) {
        final byte[] buffer = new byte[1024];
        int read;
        try {
            while ((read = input.read(buffer)) >= 0) {
                output.write(buffer, 0, read);
                output.flush();
            }
        } catch (Exception ignored) {
            // Exit quietly when either side closes.
        }
    }
}
