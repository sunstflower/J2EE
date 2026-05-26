package com.sunsetflower.macproxy.localapi.service.dto;

public record SubscriptionRecord(
        long id,
        String name,
        String sourceUrl,
        boolean enabled,
        String status,
        String lastSync
) {
}
