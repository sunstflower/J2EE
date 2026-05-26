package com.sunsetflower.macproxy.localapi.service;

import com.sunsetflower.macproxy.localapi.config.ClashMetaProperties;
import com.sunsetflower.macproxy.localapi.service.dto.CoreStatusResponse;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;

@Service
public class CoreManagerService {

    private final ClashMetaProperties clashMetaProperties;

    private volatile String state = "NOT_CONFIGURED";
    private volatile String lastError = "";
    private volatile String lastAction = "NONE";
    private volatile String lastStartedAt = "";

    public CoreManagerService(ClashMetaProperties clashMetaProperties) {
        this.clashMetaProperties = clashMetaProperties;
    }

    public CoreStatusResponse getStatus() {
        String configuredPath = clashMetaProperties.getPath();
        boolean configured = configuredPath != null && !configuredPath.isBlank();
        boolean exists = configured && Files.exists(Path.of(configuredPath));

        String effectiveState = state;
        if (!configured) {
          effectiveState = "NOT_CONFIGURED";
        } else if (!exists) {
          effectiveState = "MISSING_BINARY";
        }

        return new CoreStatusResponse(
                effectiveState,
                configuredPath,
                exists,
                lastAction,
                lastStartedAt,
                lastError
        );
    }

    public CoreStatusResponse start() {
        String configuredPath = clashMetaProperties.getPath();
        if (configuredPath == null || configuredPath.isBlank()) {
            state = "NOT_CONFIGURED";
            lastError = "Clash.Meta path is not configured";
            lastAction = "START";
            return getStatus();
        }

        if (!Files.exists(Path.of(configuredPath))) {
            state = "MISSING_BINARY";
            lastError = "Configured Clash.Meta binary does not exist";
            lastAction = "START";
            return getStatus();
        }

        state = "IDLE";
        lastError = "";
        lastAction = "START";
        lastStartedAt = OffsetDateTime.now().toString();
        return getStatus();
    }

    public CoreStatusResponse stop() {
        state = "STOPPED";
        lastAction = "STOP";
        return getStatus();
    }

    public CoreStatusResponse reload() {
        lastAction = "RELOAD";
        if ("NOT_CONFIGURED".equals(state) || "MISSING_BINARY".equals(state)) {
            return getStatus();
        }

        state = "IDLE";
        return getStatus();
    }
}
