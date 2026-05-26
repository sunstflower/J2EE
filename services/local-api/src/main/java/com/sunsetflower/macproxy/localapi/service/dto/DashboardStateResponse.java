package com.sunsetflower.macproxy.localapi.service.dto;

import java.util.List;

public record DashboardStateResponse(
        List<NavItemResponse> navItems,
        List<MetricResponse> metrics,
        List<ProxyGroupResponse> proxyGroups,
        List<SubscriptionResponse> subscriptions
) {
}
