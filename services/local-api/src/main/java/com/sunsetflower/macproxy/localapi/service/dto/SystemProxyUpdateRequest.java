package com.sunsetflower.macproxy.localapi.service.dto;

import java.util.List;

public record SystemProxyUpdateRequest(
        boolean enabled,
        String scope,
        List<String> services,
        boolean acceptRecommendedServices
) {
}
