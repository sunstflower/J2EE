package com.sunsetflower.macproxy.localapi.service.dto;

public record SettingsResponse(
        boolean systemProxyEnabled,
        boolean launchAtLogin,
        String logLevel
) {
}
