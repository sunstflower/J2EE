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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CoreConfigGenerationIntegrationTest {

    private static Path runtimeRoot;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) throws Exception {
        runtimeRoot = Files.createTempDirectory("core-config-generation-test");
        registry.add("app.runtime.root", () -> runtimeRoot.toString());
        registry.add("app.core.clash-meta.path", () -> "/tmp/nonexistent-clash-meta");
    }

    @Autowired
    private SubscriptionsService subscriptionsService;

    @Autowired
    private ProxyGroupsService proxyGroupsService;

    @Autowired
    private CoreManagerService coreManagerService;

    @Test
    void renderedConfigIncludesImportedProxiesAndSelectedGroups() throws Exception {
        var created = subscriptionsService.createSubscription(
                new SubscriptionRequest("Fixture", "file:///tmp/fixture-subscription.yaml", true)
        );
        subscriptionsService.refreshSubscription(created.id());
        proxyGroupsService.updateSelection("Global", "JP-Test-2");

        Path configPath = coreManagerService.renderConfigForCurrentState();
        String config = Files.readString(configPath);

        assertTrue(config.contains("proxies:"));
        assertTrue(config.contains("name: \"JP-Test-2\""));
        assertTrue(config.contains("name: \"US-Test-1\""));
        assertTrue(config.contains("proxy-groups:"));
        assertTrue(config.contains("name: \"Global\""));
        assertTrue(config.contains("      - \"JP-Test-2\""));
        assertTrue(config.contains("uuid: 00000000-0000-0000-0000-000000000000"));
        assertTrue(config.contains("password: \"fixture-password\""));
        assertTrue(config.contains("network: ws"));
        assertTrue(config.contains("servername: \"jp.example.com\""));
        assertTrue(config.contains("path: \"/proxy\""));
        assertTrue(config.contains("Host: \"ws.jp.example.com\""));
    }

    @Test
    void updatingSelectionWhileCoreIsStoppedPreservesStoppedState() {
        var created = subscriptionsService.createSubscription(
                new SubscriptionRequest("Fixture", "file:///tmp/fixture-subscription.yaml", true)
        );
        subscriptionsService.refreshSubscription(created.id());

        var updated = proxyGroupsService.updateSelection("Global", "JP-Test-2");
        assertEquals("JP-Test-2", updated.selectedNodeName());

        var status = coreManagerService.getStatus();
        assertEquals("MISSING_BINARY", status.state());
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
