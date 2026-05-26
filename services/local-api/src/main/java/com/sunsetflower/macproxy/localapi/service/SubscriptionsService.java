package com.sunsetflower.macproxy.localapi.service;

import com.sunsetflower.macproxy.localapi.dao.SubscriptionsDao;
import com.sunsetflower.macproxy.localapi.service.dto.SubscriptionRecord;
import com.sunsetflower.macproxy.localapi.service.dto.SubscriptionRequest;
import com.sunsetflower.macproxy.localapi.service.dto.SubscriptionResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubscriptionsService {

    private final JdbcTemplate jdbcTemplate;
    private final SubscriptionsDao subscriptionsDao;

    public SubscriptionsService(JdbcTemplate jdbcTemplate, SubscriptionsDao subscriptionsDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.subscriptionsDao = subscriptionsDao;
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
        SubscriptionRecord record = new SubscriptionRecord(
                0L,
                request.name(),
                request.sourceUrl(),
                request.enabled(),
                request.enabled() ? "Healthy" : "Disabled",
                "Never synced"
        );
        subscriptionsDao.insert(record);
        return toResponse(record);
    }

    public SubscriptionResponse updateSubscription(long subscriptionId, SubscriptionRequest request) {
        SubscriptionRecord existing = subscriptionsDao.findById(subscriptionId);
        if (existing == null) {
            throw new IllegalArgumentException("Subscription not found: " + subscriptionId);
        }

        SubscriptionRecord updated = new SubscriptionRecord(
                subscriptionId,
                request.name(),
                request.sourceUrl(),
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
