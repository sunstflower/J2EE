package com.sunsetflower.macproxy.localapi.controller;

import com.sunsetflower.macproxy.localapi.service.SubscriptionContentFetcher;
import com.sunsetflower.macproxy.localapi.service.dto.SubscriptionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SubscriptionsControllerIntegrationTest {

    private static Path runtimeRoot;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) throws Exception {
        runtimeRoot = Files.createTempDirectory("subscriptions-controller-test");
        registry.add("app.runtime.root", () -> runtimeRoot.toString());
        registry.add("app.session.token", () -> "test-session-token");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.sunsetflower.macproxy.localapi.service.SubscriptionsService subscriptionsService;

    @Test
    void refreshSubscriptionEndpointReturnsImportedSubscription() throws Exception {
        var created = subscriptionsService.createSubscription(
                new SubscriptionRequest("Fixture", "file:///tmp/fixture-subscription.yaml", true)
        );

        mockMvc.perform(post("/api/v1/subscriptions/{subscriptionId}/refresh", created.id())
                        .header("Authorization", "Bearer test-session-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("Imported 1 nodes"));
    }

    @Test
    void refreshEnabledSubscriptionsEndpointReturnsUpdatedList() throws Exception {
        subscriptionsService.createSubscription(
                new SubscriptionRequest("Fixture", "file:///tmp/fixture-subscription.yaml", true)
        );

        mockMvc.perform(post("/api/v1/subscriptions/refresh")
                        .header("Authorization", "Bearer test-session-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("Imported 1 nodes"));
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
                            """;
                }
            };
        }
    }
}
