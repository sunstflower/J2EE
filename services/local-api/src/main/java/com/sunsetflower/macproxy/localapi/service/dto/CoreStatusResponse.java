package com.sunsetflower.macproxy.localapi.service.dto;

public record CoreStatusResponse(
        String state,
        String configuredPath,
        boolean binaryExists,
        String lastAction,
        String lastStartedAt,
        String lastError
) {
}
