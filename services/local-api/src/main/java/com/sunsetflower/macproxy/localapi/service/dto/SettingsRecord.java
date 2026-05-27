package com.sunsetflower.macproxy.localapi.service.dto;

public record SettingsRecord(
        boolean systemProxyEnabled,
        String systemProxyScope,
        String systemProxyServices,
        String systemProxyConfirmedServices,
        boolean launchAtLogin,
        String logLevel
) {
}
