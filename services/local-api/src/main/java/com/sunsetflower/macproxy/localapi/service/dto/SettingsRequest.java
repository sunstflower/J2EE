package com.sunsetflower.macproxy.localapi.service.dto;

public record SettingsRequest(
        boolean systemProxyEnabled,
        String systemProxyScope,
        String systemProxyServices,
        String systemProxyConfirmedServices,
        boolean launchAtLogin,
        String logLevel
) {
}
