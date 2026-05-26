package com.sunsetflower.macproxy.localapi.controller;

import com.sunsetflower.macproxy.localapi.service.SubscriptionsService;
import com.sunsetflower.macproxy.localapi.service.dto.SubscriptionRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SubscriptionsController {

    private final SubscriptionsService subscriptionsService;

    public SubscriptionsController(SubscriptionsService subscriptionsService) {
        this.subscriptionsService = subscriptionsService;
    }

    @GetMapping("/subscriptions")
    public Map<String, Object> getSubscriptions() {
        return Map.of(
                "success", true,
                "data", subscriptionsService.getSubscriptions()
        );
    }

    @PostMapping("/subscriptions")
    public Map<String, Object> createSubscription(@RequestBody SubscriptionRequest request) {
        return Map.of(
                "success", true,
                "data", subscriptionsService.createSubscription(request)
        );
    }

    @PutMapping("/subscriptions/{subscriptionId}")
    public Map<String, Object> updateSubscription(
            @PathVariable long subscriptionId,
            @RequestBody SubscriptionRequest request
    ) {
        return Map.of(
                "success", true,
                "data", subscriptionsService.updateSubscription(subscriptionId, request)
        );
    }

    @DeleteMapping("/subscriptions/{subscriptionId}")
    public Map<String, Object> deleteSubscription(@PathVariable long subscriptionId) {
        subscriptionsService.deleteSubscription(subscriptionId);
        return Map.of(
                "success", true,
                "data", Map.of("deleted", true)
        );
    }
}
