package com.sunsetflower.macproxy.localapi.service;

import com.sunsetflower.macproxy.localapi.service.dto.DashboardStateResponse;
import com.sunsetflower.macproxy.localapi.service.dto.MetricResponse;
import com.sunsetflower.macproxy.localapi.service.dto.NavItemResponse;
import com.sunsetflower.macproxy.localapi.service.dto.ProxyGroupResponse;
import com.sunsetflower.macproxy.localapi.service.dto.SubscriptionResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final RuntimeService runtimeService;
    private final SubscriptionsService subscriptionsService;
    private final ProxyGroupsService proxyGroupsService;

    public DashboardService(
            RuntimeService runtimeService,
            SubscriptionsService subscriptionsService,
            ProxyGroupsService proxyGroupsService
    ) {
        this.runtimeService = runtimeService;
        this.subscriptionsService = subscriptionsService;
        this.proxyGroupsService = proxyGroupsService;
    }

    public DashboardStateResponse getDashboardState() {
        var runtime = runtimeService.getRuntimeSummary();
        var subscriptions = subscriptionsService.getSubscriptions();
        return new DashboardStateResponse(
                List.of(
                        new NavItemResponse("overview", "Overview"),
                        new NavItemResponse("proxies", "Proxies"),
                        new NavItemResponse("subscriptions", "Subscriptions"),
                        new NavItemResponse("settings", "Settings")
                ),
                List.of(
                        new MetricResponse("Core status", runtime.coreStatus(), "text-emerald-700 bg-emerald-50 border-emerald-200"),
                        new MetricResponse("System proxy", runtime.systemProxyStatus(), "text-amber-800 bg-amber-50 border-amber-200"),
                        new MetricResponse("Local API", runtime.backendStatus(), "text-sky-700 bg-sky-50 border-sky-200")
                )
                ,
                proxyGroupsService.buildDashboardGroups(),
                subscriptions
        );
    }
}
