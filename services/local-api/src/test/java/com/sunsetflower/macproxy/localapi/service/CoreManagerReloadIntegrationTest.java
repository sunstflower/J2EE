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
class CoreManagerReloadIntegrationTest {

    private static Path runtimeRoot;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) throws Exception {
        runtimeRoot = Files.createTempDirectory("core-reload-test");
        registry.add("app.runtime.root", () -> runtimeRoot.toString());
        registry.add("app.core.clash-meta.path", () -> "/Users/sunsetflower/myJobs/Java/mac-proxy-client/runtime-assets/clash-meta/bin/clash-meta");
    }

    @Autowired
    private SubscriptionsService subscriptionsService;

    @Autowired
    private CoreManagerService coreManagerService;

    @Test
    void reloadRewritesConfigUsingLatestImportedNodeFields() throws Exception {
        var created = subscriptionsService.createSubscription(
                new SubscriptionRequest("Fixture", "file:///tmp/fixture-subscription.yaml", true)
        );

        Files.writeString(
                Path.of("/tmp/fixture-subscription.yaml"),
                """
                        proxies:
                          - name: "US-Test-1"
                            type: ss
                            server: us1.example.com
                            port: 443
                        """.stripIndent()
        );
        subscriptionsService.refreshSubscription(created.id());
        coreManagerService.start();

        Files.writeString(
                Path.of("/tmp/fixture-subscription.yaml"),
                """
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
                        """.stripIndent()
        );

        subscriptionsService.refreshSubscription(created.id());
        var status = coreManagerService.reload();

        String config = Files.readString(runtimeRoot.resolve("clash-meta").resolve("config").resolve("config.yaml"));
        assertEquals("RUNNING", status.state());
        assertEquals("RELOAD", status.lastAction());
        assertTrue(config.contains("password: \"fixture-password\""));
        assertTrue(config.contains("tls: true"));
        assertTrue(config.contains("network: ws"));
        assertTrue(config.contains("servername: \"jp.example.com\""));
        assertTrue(config.contains("Host: \"ws.jp.example.com\""));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        SubscriptionContentFetcher subscriptionContentFetcher() {
            return new SubscriptionContentFetcher();
        }
    }
}
