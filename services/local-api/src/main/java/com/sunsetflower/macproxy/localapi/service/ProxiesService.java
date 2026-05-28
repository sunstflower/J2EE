package com.sunsetflower.macproxy.localapi.service;

import com.sunsetflower.macproxy.localapi.service.dto.ImportedProxyNodeResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProxiesService {

    private final ImportedProxyNodesService importedProxyNodesService;

    public ProxiesService(ImportedProxyNodesService importedProxyNodesService) {
        this.importedProxyNodesService = importedProxyNodesService;
    }

    public List<ImportedProxyNodeResponse> getImportedNodes() {
        return importedProxyNodesService.getAllNodes().stream()
                .map(node -> new ImportedProxyNodeResponse(
                        node.id(),
                        node.subscriptionId(),
                        node.nodeName(),
                        node.nodeType(),
                        node.server(),
                        node.port(),
                        node.cipher(),
                        node.password(),
                        node.uuid(),
                        node.alterId(),
                        node.tls(),
                        node.network(),
                        node.serverName(),
                        node.wsPath(),
                        node.wsHost(),
                        node.importedAt()
                ))
                .toList();
    }
}
