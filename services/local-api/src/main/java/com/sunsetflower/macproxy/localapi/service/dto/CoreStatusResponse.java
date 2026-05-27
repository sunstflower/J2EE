package com.sunsetflower.macproxy.localapi.service.dto;

public record CoreStatusResponse(
        String state,
        String configuredPath,
        boolean binaryExists,
        int mixedPort,
        int controllerPort,
        String lastAction,
        String lastStartedAt,
        String lastError
) {
}
