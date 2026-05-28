package com.sunsetflower.macproxy.localapi.service;

import com.sunsetflower.macproxy.localapi.dao.SubscriptionsDao;
import com.sunsetflower.macproxy.localapi.service.dto.ImportedProxyNodeRecord;
import com.sunsetflower.macproxy.localapi.service.dto.SubscriptionRecord;
import com.sunsetflower.macproxy.localapi.service.dto.SubscriptionRequest;
import com.sunsetflower.macproxy.localapi.service.dto.SubscriptionResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
public class SubscriptionsService {

    private final JdbcTemplate jdbcTemplate;
    private final SubscriptionsDao subscriptionsDao;
    private final ImportedProxyNodesService importedProxyNodesService;
    private final SubscriptionContentFetcher subscriptionContentFetcher;
    private final Yaml yaml;

    public SubscriptionsService(
            JdbcTemplate jdbcTemplate,
            SubscriptionsDao subscriptionsDao,
            ImportedProxyNodesService importedProxyNodesService,
            SubscriptionContentFetcher subscriptionContentFetcher
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.subscriptionsDao = subscriptionsDao;
        this.importedProxyNodesService = importedProxyNodesService;
        this.subscriptionContentFetcher = subscriptionContentFetcher;
        this.yaml = new Yaml();
    }

    @PostConstruct
    public void initializeSchema() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS subscriptions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    source_url TEXT NOT NULL,
                    enabled INTEGER NOT NULL,
                    status TEXT NOT NULL,
                    last_sync TEXT NOT NULL
                )
                """);
    }

    public List<SubscriptionResponse> getSubscriptions() {
        return subscriptionsDao.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public SubscriptionResponse createSubscription(SubscriptionRequest request) {
        validateRequest(request);
        SubscriptionRecord record = new SubscriptionRecord(
                0L,
                request.name().trim(),
                request.sourceUrl().trim(),
                request.enabled(),
                request.enabled() ? "Healthy" : "Disabled",
                "Never synced"
        );
        subscriptionsDao.insert(record);
        SubscriptionRecord created = subscriptionsDao.findById(subscriptionsDao.lastInsertedId());
        return toResponse(created);
    }

    public SubscriptionResponse updateSubscription(long subscriptionId, SubscriptionRequest request) {
        validateRequest(request);
        SubscriptionRecord existing = subscriptionsDao.findById(subscriptionId);
        if (existing == null) {
            throw new IllegalArgumentException("Subscription not found: " + subscriptionId);
        }

        SubscriptionRecord updated = new SubscriptionRecord(
                subscriptionId,
                request.name().trim(),
                request.sourceUrl().trim(),
                request.enabled(),
                request.enabled() ? existing.status() : "Disabled",
                existing.lastSync()
        );
        subscriptionsDao.update(updated);
        return toResponse(updated);
    }

    public void deleteSubscription(long subscriptionId) {
        subscriptionsDao.delete(subscriptionId);
    }

    public SubscriptionResponse refreshSubscription(long subscriptionId) {
        SubscriptionRecord existing = subscriptionsDao.findById(subscriptionId);
        if (existing == null) {
            throw new IllegalArgumentException("Subscription not found: " + subscriptionId);
        }

        if (!existing.enabled()) {
            SubscriptionRecord disabled = new SubscriptionRecord(
                    existing.id(),
                    existing.name(),
                    existing.sourceUrl(),
                    false,
                    "Disabled",
                    existing.lastSync()
            );
            subscriptionsDao.update(disabled);
            return toResponse(disabled);
        }

        ImportedSubscription imported = fetchSubscription(existing);
        importedProxyNodesService.replaceNodesForSubscription(existing.id(), imported.nodes());

        SubscriptionRecord refreshed = new SubscriptionRecord(
                existing.id(),
                existing.name(),
                existing.sourceUrl(),
                true,
                imported.status(),
                OffsetDateTime.now().toString()
        );
        subscriptionsDao.update(refreshed);
        return toResponse(refreshed);
    }

    public List<SubscriptionResponse> refreshEnabledSubscriptions() {
        return subscriptionsDao.findAll().stream()
                .filter(SubscriptionRecord::enabled)
                .map(subscription -> refreshSubscription(subscription.id()))
                .toList();
    }

    private void validateRequest(SubscriptionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Subscription request is required");
        }

        String name = request.name() == null ? "" : request.name().trim();
        if (name.isBlank()) {
            throw new IllegalArgumentException("Subscription name is required");
        }

        String sourceUrl = request.sourceUrl() == null ? "" : request.sourceUrl().trim();
        if (sourceUrl.isBlank()) {
            throw new IllegalArgumentException("Subscription URL is required");
        }

        try {
            URI uri = new URI(sourceUrl);
            String scheme = uri.getScheme();
            if (scheme == null || (
                    !"file".equalsIgnoreCase(scheme)
                            && !"http".equalsIgnoreCase(scheme)
                            && !"https".equalsIgnoreCase(scheme)
            )) {
                throw new IllegalArgumentException("Subscription URL must use http, https, or file");
            }
            if ("file".equalsIgnoreCase(scheme) && (uri.getPath() == null || uri.getPath().isBlank())) {
                throw new IllegalArgumentException("Subscription file URL must include a path");
            }
            if (("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    && (uri.getHost() == null || uri.getHost().isBlank())) {
                throw new IllegalArgumentException("Subscription URL must include a host");
            }
        } catch (URISyntaxException error) {
            throw new IllegalArgumentException("Subscription URL is invalid");
        }
    }

    private ImportedSubscription fetchSubscription(SubscriptionRecord subscription) {
        String body = subscriptionContentFetcher.fetch(subscription.sourceUrl());
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Subscription payload is empty");
        }

        List<ImportedProxyNodeRecord> nodes = parseImportedNodes(subscription.id(), body);
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("Subscription payload does not contain any proxies");
        }

        return new ImportedSubscription("Imported " + nodes.size() + " nodes", nodes);
    }

    private List<ImportedProxyNodeRecord> parseImportedNodes(long subscriptionId, String body) {
        Object parsedDocument;
        try {
            parsedDocument = yaml.load(body);
        } catch (RuntimeException error) {
            throw new IllegalArgumentException("Subscription payload is not valid YAML");
        }

        if (!(parsedDocument instanceof Map<?, ?> root)) {
            return List.of();
        }

        Object proxiesValue = root.get("proxies");
        if (!(proxiesValue instanceof List<?> proxyEntries)) {
            return List.of();
        }

        String importedAt = OffsetDateTime.now().toString();
        return proxyEntries.stream()
                .map(entry -> toImportedNode(subscriptionId, asObjectMap(entry), importedAt))
                .filter(node -> node != null)
                .toList();
    }

    private ImportedProxyNodeRecord toImportedNode(long subscriptionId, Map<String, Object> values, String importedAt) {
        String nodeName = readString(values, "name");
        String nodeType = readString(values, "type");
        String server = readString(values, "server");
        int port = parsePort(readString(values, "port"));

        if (nodeName.isBlank() || nodeType.isBlank() || server.isBlank() || port <= 0) {
            return null;
        }

        Map<String, Object> wsOptions = asObjectMap(values.get("ws-opts"));
        Map<String, Object> wsHeaders = asObjectMap(wsOptions.get("headers"));
        return new ImportedProxyNodeRecord(
                0L,
                subscriptionId,
                nodeName,
                nodeType,
                server,
                port,
                nullable(readString(values, "cipher")),
                nullable(readString(values, "password")),
                nullable(readString(values, "uuid")),
                parseNullableInteger(readString(values, "alterId")),
                parseNullableBoolean(readString(values, "tls")),
                nullable(readString(values, "network")),
                nullable(firstNonBlank(
                        readString(values, "servername"),
                        readString(values, "serverName"),
                        readString(values, "sni")
                )),
                nullable(readString(wsOptions, "path")),
                nullable(firstNonBlank(
                        readString(wsHeaders, "Host"),
                        readString(wsHeaders, "host")
                )),
                importedAt
        );
    }

    private int parsePort(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException error) {
            return 0;
        }
    }

    private Integer parseNullableInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException error) {
            return null;
        }
    }

    private Boolean parseNullableBoolean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Boolean.parseBoolean(value.trim());
    }

    private Map<String, Object> asObjectMap(Object value) {
        if (value instanceof Map<?, ?> rawMap) {
            java.util.LinkedHashMap<String, Object> result = new java.util.LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                if (entry.getKey() == null) {
                    continue;
                }
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return Map.copyOf(result);
        }
        return Map.of();
    }

    private String readString(Map<String, Object> values, String key) {
        Object value = values.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String nullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate.trim();
            }
        }
        return "";
    }

    private record ImportedSubscription(String status, List<ImportedProxyNodeRecord> nodes) {
    }

    private SubscriptionResponse toResponse(SubscriptionRecord record) {
        return new SubscriptionResponse(
                record.id(),
                record.name(),
                record.sourceUrl(),
                record.enabled(),
                record.status(),
                record.lastSync()
        );
    }
}
