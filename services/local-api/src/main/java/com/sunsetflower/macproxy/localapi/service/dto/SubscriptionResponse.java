package com.sunsetflower.macproxy.localapi.service.dto;

public record SubscriptionResponse(
        String name,
        String status,
        String lastSync
) {
}
