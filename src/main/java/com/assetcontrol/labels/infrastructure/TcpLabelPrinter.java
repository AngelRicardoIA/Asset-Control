package com.assetcontrol.labels.infrastructure;

import com.assetcontrol.labels.application.LabelContent;
import com.assetcontrol.labels.application.LabelPrinter;
import com.assetcontrol.labels.application.LabelPrintingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class TcpLabelPrinter implements LabelPrinter {

    private final ZplLabelRenderer renderer;
    private final int port;
    private final int timeoutMs;
    private final Set<String> allowedPrinterIps;

    public TcpLabelPrinter(
            ZplLabelRenderer renderer,
            @Value("${asset-control.labels.printer-port:9100}") int port,
            @Value("${asset-control.labels.timeout-ms:5000}") int timeoutMs,
            @Value("${asset-control.labels.allowed-printer-ips:}") String allowedPrinterIps
    ) {
        if (port < 1 || port > 65535 || timeoutMs < 100 || timeoutMs > 30000) {
            throw new IllegalArgumentException("Revisa el puerto y el tiempo de espera de la impresora.");
        }
        this.renderer = renderer;
        this.port = port;
        this.timeoutMs = timeoutMs;
        this.allowedPrinterIps = Arrays.stream(allowedPrinterIps.split(","))
                .map(String::strip).filter(value -> !value.isEmpty()).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void print(String printerIp, LabelContent content) {
        byte[] address = printerAddress(printerIp);
        byte[] zpl = renderer.render(content);
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);

        try (var channel = SocketChannel.open(); var selector = Selector.open()) {
            channel.configureBlocking(false);
            var target = new InetSocketAddress(InetAddress.getByAddress(address), port);
            boolean connected = channel.connect(target);
            var key = channel.register(selector, connected ? SelectionKey.OP_WRITE : SelectionKey.OP_CONNECT);
            while (!connected) {
                awaitReady(selector, deadline);
                connected = channel.finishConnect();
            }
            key.interestOps(SelectionKey.OP_WRITE);
            var buffer = ByteBuffer.wrap(zpl);
            while (buffer.hasRemaining()) {
                if (System.nanoTime() >= deadline) {
                    throw new SocketTimeoutException("Tiempo de envío agotado.");
                }
                if (channel.write(buffer) == 0) {
                    awaitReady(selector, deadline);
                }
            }
        } catch (IOException exception) {
            throw new LabelPrintingException(
                    "No se pudo confirmar el envío. Revisa la IP, la conexión y la impresora antes de volver a imprimir.",
                    exception
            );
        }
    }

    private byte[] printerAddress(String printerIp) {
        String ip = printerIp == null ? "" : printerIp.strip();
        if (!ip.matches("(?:0|[1-9][0-9]{0,2})(?:\\.(?:0|[1-9][0-9]{0,2})){3}")) {
            throw new IllegalArgumentException("Escribe una dirección IPv4 válida para la impresora.");
        }
        int[] octets = Arrays.stream(ip.split("\\.")).mapToInt(Integer::parseInt).toArray();
        if (Arrays.stream(octets).anyMatch(value -> value > 255)) {
            throw new IllegalArgumentException("Escribe una dirección IPv4 válida para la impresora.");
        }
        boolean privateNetwork = octets[0] == 10
                || (octets[0] == 172 && octets[1] >= 16 && octets[1] <= 31)
                || (octets[0] == 192 && octets[1] == 168);
        boolean allowed = allowedPrinterIps.isEmpty() ? privateNetwork : allowedPrinterIps.contains(ip);
        if (!allowed) {
            throw new IllegalArgumentException("Esta IP no está habilitada para imprimir etiquetas en esta instalación.");
        }
        return new byte[] {(byte) octets[0], (byte) octets[1], (byte) octets[2], (byte) octets[3]};
    }

    private void awaitReady(Selector selector, long deadline) throws IOException {
        long remaining = deadline - System.nanoTime();
        if (remaining <= 0) {
            throw new SocketTimeoutException("Tiempo de envío agotado.");
        }
        selector.select(Math.max(1, TimeUnit.NANOSECONDS.toMillis(remaining)));
        selector.selectedKeys().clear();
    }
}
