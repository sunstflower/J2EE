package com.sunsetflower.macproxy.localapi.service.dto;

public record RuntimeSummaryResponse(
        String backendStatus,
        String coreStatus,
        String systemProxyStatus,
        int subscriptionCount,
        String logLevel
) {
}
