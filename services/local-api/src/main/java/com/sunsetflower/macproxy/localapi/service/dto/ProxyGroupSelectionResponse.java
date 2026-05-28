package com.sunsetflower.macproxy.localapi.service.dto;

import java.util.List;

public record ProxyGroupSelectionResponse(
        String groupName,
        String selectedNodeName,
        List<String> availableNodeNames,
        String updatedAt
) {
}
