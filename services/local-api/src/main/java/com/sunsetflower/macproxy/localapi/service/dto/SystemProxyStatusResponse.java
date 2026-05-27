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
        List<String> confirmedServices,
        List<String> recommendedServices,
        List<String> availableServices,
        List<String> activeServices,
        boolean recommendationPending,
        String targetHost,
        int targetPort,
        int serviceCount,
        List<String> services,
        String lastAction,
        String lastError
) {
}
