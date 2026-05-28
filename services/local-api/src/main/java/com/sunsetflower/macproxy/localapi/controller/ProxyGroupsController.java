package com.sunsetflower.macproxy.localapi.controller;

import com.sunsetflower.macproxy.localapi.service.CoreManagerService;
import com.sunsetflower.macproxy.localapi.service.ProxyGroupsService;
import com.sunsetflower.macproxy.localapi.service.dto.ProxyGroupSelectionRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ProxyGroupsController {

    private final ProxyGroupsService proxyGroupsService;
    private final CoreManagerService coreManagerService;

    public ProxyGroupsController(ProxyGroupsService proxyGroupsService, CoreManagerService coreManagerService) {
        this.proxyGroupsService = proxyGroupsService;
        this.coreManagerService = coreManagerService;
    }

    @GetMapping("/proxies/groups")
    public Map<String, Object> getGroups() {
        return Map.of(
                "success", true,
                "data", proxyGroupsService.getGroups()
        );
    }

    @PutMapping("/proxies/groups/{groupName}/selection")
    public Map<String, Object> updateSelection(
            @PathVariable String groupName,
            @RequestBody ProxyGroupSelectionRequest request
    ) {
        var updated = proxyGroupsService.updateSelection(groupName, request.selectedNodeName());
        return Map.of(
                "success", true,
                "data", updated,
                "core", coreManagerService.getStatus()
        );
    }
}
