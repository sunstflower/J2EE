package com.sunsetflower.macproxy.localapi.service.dto;

public record SettingsRecord(
        boolean systemProxyEnabled,
        boolean launchAtLogin,
        String logLevel
) {
}
