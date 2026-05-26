package com.sunsetflower.macproxy.localapi.service.dto;

public record SessionMetadataResponse(
        String appName,
        String backendVersion,
        String runtimeMode,
        String authenticatedAt
) {
}
