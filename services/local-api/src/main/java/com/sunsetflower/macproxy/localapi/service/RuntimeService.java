package com.sunsetflower.macproxy.localapi.service;

import com.sunsetflower.macproxy.localapi.service.dto.RuntimeSummaryResponse;
import org.springframework.stereotype.Service;

@Service
public class RuntimeService {

    private final SettingsService settingsService;
    private final SubscriptionsService subscriptionsService;
    private final CoreManagerService coreManagerService;
    private final SystemProxyService systemProxyService;

    public RuntimeService(
            SettingsService settingsService,
            SubscriptionsService subscriptionsService,
            CoreManagerService coreManagerService,
            SystemProxyService systemProxyService
    ) {
        this.settingsService = settingsService;
        this.subscriptionsService = subscriptionsService;
        this.coreManagerService = coreManagerService;
        this.systemProxyService = systemProxyService;
    }

    public RuntimeSummaryResponse getRuntimeSummary() {
        var settings = settingsService.getSettings();
        var subscriptions = subscriptionsService.getSubscriptions();
        var coreStatus = coreManagerService.getStatus();
        var systemProxyStatus = systemProxyService.getStatus();

        return new RuntimeSummaryResponse(
                "UP",
                coreStatus.state(),
                systemProxyStatus.statusLabel(),
                subscriptions.size(),
                settings.logLevel()
        );
    }
}
