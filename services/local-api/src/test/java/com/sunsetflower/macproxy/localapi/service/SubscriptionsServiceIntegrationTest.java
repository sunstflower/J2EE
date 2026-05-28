package com.sunsetflower.macproxy.localapi.service;

import com.sunsetflower.macproxy.localapi.service.dto.SubscriptionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SubscriptionsServiceIntegrationTest {

    private static Path runtimeRoot;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) throws Exception {
        runtimeRoot = Files.createTempDirectory("subscriptions-service-test");
        registry.add("app.runtime.root", () -> runtimeRoot.toString());
    }

    @Autowired
    private SubscriptionsService subscriptionsService;

    @Autowired
    private ImportedProxyNodesService importedProxyNodesService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ProxyGroupsService proxyGroupsService;

    @Test
    void refreshImportsRemoteProxyNodesAndSurfacesThemInDashboard() {
        var created = subscriptionsService.createSubscription(
                new SubscriptionRequest("Fixture", "file:///tmp/fixture-subscription.yaml", true)
        );
        assertTrue(created.id() > 0);

        var refreshed = subscriptionsService.refreshSubscription(created.id());
        assertEquals("Imported 2 nodes", refreshed.status());
        assertFalse(refreshed.lastSync().isBlank());

        var nodes = importedProxyNodesService.getNodesForSubscription(created.id());
        assertEquals(2, nodes.size());
        assertEquals("JP-Test-2", nodes.get(0).nodeName());
        assertEquals("auto", nodes.get(0).cipher());
        assertEquals("00000000-0000-0000-0000-000000000000", nodes.get(0).uuid());
        assertEquals("ws", nodes.get(0).network());
        assertEquals("jp.example.com", nodes.get(0).serverName());
        assertEquals("/proxy", nodes.get(0).wsPath());
        assertEquals("ws.jp.example.com", nodes.get(0).wsHost());
        assertEquals("US-Test-1", nodes.get(1).nodeName());
        assertEquals("aes-128-gcm", nodes.get(1).cipher());
        assertEquals("fixture-password", nodes.get(1).password());

        var dashboard = dashboardService.getDashboardState();
        assertEquals("Auto Select", dashboard.proxyGroups().get(0).name());
        assertEquals("(not selected)", dashboard.proxyGroups().get(0).active());
        assertEquals("Global", dashboard.proxyGroups().get(1).name());
        assertEquals("(not selected)", dashboard.proxyGroups().get(1).active());
    }

    @Test
    void proxyGroupSelectionPersistsImportedNodeChoice() {
        var created = subscriptionsService.createSubscription(
                new SubscriptionRequest("Fixture", "file:///tmp/fixture-subscription.yaml", true)
        );
        subscriptionsService.refreshSubscription(created.id());

        var updated = proxyGroupsService.updateSelection("Global", "JP-Test-2");
        assertEquals("Global", updated.groupName());
        assertEquals("JP-Test-2", updated.selectedNodeName());
        assertTrue(updated.availableNodeNames().contains("JP-Test-2"));

        var groups = proxyGroupsService.getGroups();
        var persisted = groups.stream()
                .filter(group -> "Global".equals(group.groupName()))
                .findFirst()
                .orElseThrow();
        assertEquals("JP-Test-2", persisted.selectedNodeName());
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        SubscriptionContentFetcher subscriptionContentFetcher() {
            return new SubscriptionContentFetcher() {
                @Override
                public String fetch(String sourceUrl) {
                    return """
                            proxies:
                              - name: "US-Test-1"
                                type: ss
                                server: us1.example.com
                                port: 443
                                cipher: aes-128-gcm
                                password: fixture-password
                              - name: "JP-Test-2"
                                type: vmess
                                server: jp2.example.com
                                port: 8443
                                uuid: 00000000-0000-0000-0000-000000000000
                                alterId: 0
                                cipher: auto
                                tls: true
                                network: ws
                                servername: jp.example.com
                                ws-opts:
                                  path: /proxy
                                  headers:
                                    Host: ws.jp.example.com
                            proxy-groups: []
                            rules:
                              - MATCH,DIRECT
                            """;
                }
            };
        }
    }
}
