package com.sunsetflower.macproxy.localapi.service.dto;

public record ImportedProxyNodeRecord(
        long id,
        long subscriptionId,
        String nodeName,
        String nodeType,
        String server,
        int port,
        String cipher,
        String password,
        String uuid,
        Integer alterId,
        Boolean tls,
        String network,
        String serverName,
        String wsPath,
        String wsHost,
        String importedAt
) {
}
