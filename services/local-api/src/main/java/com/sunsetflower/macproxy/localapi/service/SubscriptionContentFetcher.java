package com.sunsetflower.macproxy.localapi.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Service
public class SubscriptionContentFetcher {

    private final HttpClient httpClient;

    public SubscriptionContentFetcher() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String fetch(String sourceUrl) {
        if (sourceUrl != null && sourceUrl.startsWith("file://")) {
            return readLocalFile(sourceUrl);
        }

        return fetchRemoteContent(sourceUrl);
    }

    private String readLocalFile(String sourceUrl) {
        try {
            Path path = Path.of(new URI(sourceUrl));
            return Files.readString(path);
        } catch (IOException | URISyntaxException error) {
            throw new IllegalArgumentException("Failed to read local subscription file: " + sourceUrl);
        }
    }

    private String fetchRemoteContent(String sourceUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI(sourceUrl))
                    .GET()
                    .timeout(Duration.ofSeconds(20))
                    .header("User-Agent", "mac-proxy-client/0.1")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalArgumentException(
                        "Failed to download subscription: HTTP " + response.statusCode()
                );
            }
            return response.body();
        } catch (IllegalArgumentException error) {
            throw error;
        } catch (Exception error) {
            throw new IllegalArgumentException("Failed to download subscription: " + sourceUrl);
        }
    }
}
