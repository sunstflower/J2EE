package com.sunsetflower.macproxy.localapi.controller;

import com.sunsetflower.macproxy.localapi.service.SettingsService;
import com.sunsetflower.macproxy.localapi.service.dto.SettingsRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping("/settings")
    public Map<String, Object> getSettings() {
        return Map.of(
                "success", true,
                "data", settingsService.getSettings()
        );
    }

    @PutMapping("/settings")
    public Map<String, Object> updateSettings(@RequestBody SettingsRequest request) {
        return Map.of(
                "success", true,
                "data", settingsService.updateSettings(request)
        );
    }
}
