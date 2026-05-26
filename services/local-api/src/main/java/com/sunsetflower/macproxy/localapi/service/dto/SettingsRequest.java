package com.sunsetflower.macproxy.localapi.service.dto;

public record SettingsRequest(
        boolean systemProxyEnabled,
        boolean launchAtLogin,
        String logLevel
) {
}
