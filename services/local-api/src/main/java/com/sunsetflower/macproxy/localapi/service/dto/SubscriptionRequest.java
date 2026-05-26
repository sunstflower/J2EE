package com.sunsetflower.macproxy.localapi.service.dto;

public record SubscriptionRequest(
        String name,
        String sourceUrl,
        boolean enabled
) {
}
