package com.sunsetflower.macproxy.localapi.service;

import com.sunsetflower.macproxy.localapi.service.dto.RuntimeSummaryResponse;
import org.springframework.stereotype.Service;

@Service
public class RuntimeService {

    private final SettingsService settingsService;
    private final SubscriptionsService subscriptionsService;
    private final CoreManagerService coreManagerService;

    public RuntimeService(
            SettingsService settingsService,
            SubscriptionsService subscriptionsService,
            CoreManagerService coreManagerService
    ) {
        this.settingsService = settingsService;
        this.subscriptionsService = subscriptionsService;
        this.coreManagerService = coreManagerService;
    }

    public RuntimeSummaryResponse getRuntimeSummary() {
        var settings = settingsService.getSettings();
        var subscriptions = subscriptionsService.getSubscriptions();
        var coreStatus = coreManagerService.getStatus();

        return new RuntimeSummaryResponse(
                "UP",
                coreStatus.state(),
                settings.systemProxyEnabled() ? "Enabled" : "Disabled",
                subscriptions.size(),
                settings.logLevel()
        );
    }
}
