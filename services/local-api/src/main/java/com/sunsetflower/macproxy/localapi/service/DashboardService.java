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

    public DashboardStateResponse getDashboardState() {
        return new DashboardStateResponse(
                List.of(
                        new NavItemResponse("overview", "Overview"),
                        new NavItemResponse("proxies", "Proxies"),
                        new NavItemResponse("subscriptions", "Subscriptions"),
                        new NavItemResponse("settings", "Settings")
                ),
                List.of(
                        new MetricResponse("Core status", "Idle", "text-emerald-700 bg-emerald-50 border-emerald-200"),
                        new MetricResponse("System proxy", "Disabled", "text-amber-800 bg-amber-50 border-amber-200"),
                        new MetricResponse("Local API", "Scaffolded", "text-sky-700 bg-sky-50 border-sky-200")
                ),
                List.of(
                        new ProxyGroupResponse("Auto Select", "JP-03 Tokyo", "Latency probe"),
                        new ProxyGroupResponse("Global", "US-01 Los Angeles", "Manual"),
                        new ProxyGroupResponse("Streaming", "SG-02 Singapore", "Rule-based")
                ),
                List.of(
                        new SubscriptionResponse(1L, "Primary feed", "https://example.com/primary", true, "Healthy", "5 min ago"),
                        new SubscriptionResponse(2L, "Fallback nodes", "https://example.com/fallback", false, "Pending", "Not synced yet")
                )
        );
    }
}
