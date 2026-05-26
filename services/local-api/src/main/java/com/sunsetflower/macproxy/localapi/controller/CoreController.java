package com.sunsetflower.macproxy.localapi.controller;

import com.sunsetflower.macproxy.localapi.service.CoreManagerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class CoreController {

    private final CoreManagerService coreManagerService;

    public CoreController(CoreManagerService coreManagerService) {
        this.coreManagerService = coreManagerService;
    }

    @GetMapping("/core")
    public Map<String, Object> getCoreStatus() {
        return Map.of(
                "success", true,
                "data", coreManagerService.getStatus()
        );
    }

    @PostMapping("/core/start")
    public Map<String, Object> startCore() {
        return Map.of(
                "success", true,
                "data", coreManagerService.start()
        );
    }

    @PostMapping("/core/stop")
    public Map<String, Object> stopCore() {
        return Map.of(
                "success", true,
                "data", coreManagerService.stop()
        );
    }

    @PostMapping("/core/reload")
    public Map<String, Object> reloadCore() {
        return Map.of(
                "success", true,
                "data", coreManagerService.reload()
        );
    }
}
