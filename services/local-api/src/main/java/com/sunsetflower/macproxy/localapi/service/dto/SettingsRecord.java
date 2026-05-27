package com.sunsetflower.macproxy.localapi.service.dto;

public record SettingsRecord(
        boolean systemProxyEnabled,
        String systemProxyScope,
        String systemProxyServices,
        boolean launchAtLogin,
        String logLevel
) {
}
