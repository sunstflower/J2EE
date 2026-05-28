package com.sunsetflower.macproxy.localapi.service;

import com.sunsetflower.macproxy.localapi.config.AppRuntimeProperties;
import com.sunsetflower.macproxy.localapi.service.dto.RuntimeErrorResponse;
import com.sunsetflower.macproxy.localapi.service.dto.RuntimeErrorsResponse;
import com.sunsetflower.macproxy.localapi.service.dto.RuntimeLogLineResponse;
import com.sunsetflower.macproxy.localapi.service.dto.RuntimeLogsResponse;
import com.sunsetflower.macproxy.localapi.service.dto.RuntimeSummaryResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class RuntimeService {

    private static final int DEFAULT_LOG_TAIL_LINES = 200;

    private final AppRuntimeProperties appRuntimeProperties;
    private final SettingsService settingsService;
    private final SubscriptionsService subscriptionsService;
    private final CoreManagerService coreManagerService;
    private final SystemProxyService systemProxyService;

    public RuntimeService(
            AppRuntimeProperties appRuntimeProperties,
            SettingsService settingsService,
            SubscriptionsService subscriptionsService,
            CoreManagerService coreManagerService,
            SystemProxyService systemProxyService
    ) {
        this.appRuntimeProperties = appRuntimeProperties;
        this.settingsService = settingsService;
        this.subscriptionsService = subscriptionsService;
        this.coreManagerService = coreManagerService;
        this.systemProxyService = systemProxyService;
    }

    public RuntimeSummaryResponse getRuntimeSummary() {
        var settings = settingsService.getSettings();
        var subscriptions = subscriptionsService.getSubscriptions();
        var coreStatus = coreManagerService.getStatus();
        var systemProxyStatus = systemProxyService.getStatus();

        return new RuntimeSummaryResponse(
                "UP",
                coreStatus.state(),
                systemProxyStatus.statusLabel(),
                subscriptions.size(),
                settings.logLevel()
        );
    }

    public RuntimeLogsResponse getRuntimeLogs(Integer limit) {
        Path logPath = Path.of(appRuntimeProperties.getRoot(), "clash-meta", "logs", "clash-meta.log");
        int effectiveLimit = normalizeLogLimit(limit);

        if (!Files.exists(logPath)) {
            return new RuntimeLogsResponse(
                    logPath.toString(),
                    false,
                    0,
                    List.of()
            );
        }

        try {
            List<String> allLines = Files.readAllLines(logPath, StandardCharsets.UTF_8);
            int fromIndex = Math.max(0, allLines.size() - effectiveLimit);
            List<RuntimeLogLineResponse> tailLines = java.util.stream.IntStream.range(fromIndex, allLines.size())
                    .mapToObj(index -> new RuntimeLogLineResponse(index + 1, allLines.get(index)))
                    .toList();

            return new RuntimeLogsResponse(
                    logPath.toString(),
                    true,
                    tailLines.size(),
                    tailLines
            );
        } catch (IOException error) {
            throw new IllegalStateException("Failed to read runtime log file: " + error.getMessage(), error);
        }
    }

    public RuntimeErrorsResponse getRuntimeErrors() {
        List<RuntimeErrorResponse> errors = new ArrayList<>();

        var coreStatus = coreManagerService.getStatus();
        if (coreStatus.lastError() != null && !coreStatus.lastError().isBlank()) {
            errors.add(new RuntimeErrorResponse("core", "error", coreStatus.lastError()));
        }

        var systemProxyStatus = systemProxyService.getStatus();
        if (systemProxyStatus.lastError() != null && !systemProxyStatus.lastError().isBlank()) {
            errors.add(new RuntimeErrorResponse("systemProxy", "error", systemProxyStatus.lastError()));
        }

        RuntimeLogsResponse runtimeLogs = getRuntimeLogs(50);
        for (RuntimeLogLineResponse line : runtimeLogs.lines()) {
            String normalized = line.content().toLowerCase();
            if (normalized.contains(" level=error ") || normalized.contains(" level=fatal ")) {
                String severity = normalized.contains(" level=fatal ") ? "fatal" : "error";
                errors.add(new RuntimeErrorResponse("coreLog", severity, line.content()));
            }
        }

        return new RuntimeErrorsResponse(errors.size(), errors);
    }

    private int normalizeLogLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LOG_TAIL_LINES;
        }

        if (limit < 1) {
            return 1;
        }

        return Math.min(limit, 1000);
    }
}
