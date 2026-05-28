package com.sunsetflower.macproxy.localapi.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SubscriptionContentFetcherTest {

    @TempDir
    Path tempDir;

    @Test
    void fetchReadsFileUrls() throws Exception {
        Path file = tempDir.resolve("subscription.yaml");
        Files.writeString(file, "proxies:\n  - name: Test\n");

        SubscriptionContentFetcher fetcher = new SubscriptionContentFetcher();
        String content = fetcher.fetch(file.toUri().toString());

        assertEquals("proxies:\n  - name: Test\n", content);
    }

    @Test
    void fetchReadsHttpUrls() throws Exception {
        HttpServer server = startServer(200, "proxies:\n  - name: Remote\n");
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/subscription.yaml";
            SubscriptionContentFetcher fetcher = new SubscriptionContentFetcher();

            String content = fetcher.fetch(url);

            assertEquals("proxies:\n  - name: Remote\n", content);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void fetchThrowsOnNonSuccessHttpStatus() throws Exception {
        HttpServer server = startServer(404, "missing");
        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/missing.yaml";
            SubscriptionContentFetcher fetcher = new SubscriptionContentFetcher();

            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> fetcher.fetch(url));

            assertEquals("Failed to download subscription: HTTP 404", error.getMessage());
        } finally {
            server.stop(0);
        }
    }

    private HttpServer startServer(int statusCode, String body) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> writeResponse(exchange, statusCode, body));
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
        return server;
    }

    private void writeResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] payload = body.getBytes();
        exchange.sendResponseHeaders(statusCode, payload.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(payload);
        }
    }
}
