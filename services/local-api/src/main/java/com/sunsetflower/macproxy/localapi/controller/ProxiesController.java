package com.sunsetflower.macproxy.localapi.controller;

import com.sunsetflower.macproxy.localapi.service.ProxiesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ProxiesController {

    private final ProxiesService proxiesService;

    public ProxiesController(ProxiesService proxiesService) {
        this.proxiesService = proxiesService;
    }

    @GetMapping("/proxies/nodes")
    public Map<String, Object> getImportedNodes() {
        return Map.of(
                "success", true,
                "data", proxiesService.getImportedNodes()
        );
    }
}
