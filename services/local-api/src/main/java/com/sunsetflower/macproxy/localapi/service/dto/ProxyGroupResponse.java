package com.sunsetflower.macproxy.localapi.service.dto;

public record ProxyGroupResponse(
        String name,
        String active,
        String policy
) {
}
