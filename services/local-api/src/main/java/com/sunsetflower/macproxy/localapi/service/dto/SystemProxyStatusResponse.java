package com.sunsetflower.macproxy.localapi.service.dto;

import java.util.List;

public record SystemProxyStatusResponse(
        boolean enabled,
        boolean managed,
        String mode,
        String statusLabel,
        String capability,
        String scope,
        List<String> selectedServices,
        String targetHost,
        int targetPort,
        int serviceCount,
        List<String> services,
        String lastAction,
        String lastError
) {
}
