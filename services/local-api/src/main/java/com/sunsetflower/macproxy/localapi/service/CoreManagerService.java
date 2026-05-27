package com.sunsetflower.macproxy.localapi.service;

import com.sunsetflower.macproxy.localapi.config.AppRuntimeProperties;
import com.sunsetflower.macproxy.localapi.config.ClashMetaProperties;
import com.sunsetflower.macproxy.localapi.service.dto.CoreStatusResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class CoreManagerService {

    private static final int MIXED_PORT = 7890;
    private static final int CONTROLLER_PORT = 9090;

    private final AppRuntimeProperties appRuntimeProperties;
    private final ClashMetaProperties clashMetaProperties;

    private volatile Process coreProcess;
    private volatile String state = "NOT_CONFIGURED";
    private volatile String lastError = "";
    private volatile String lastAction = "NONE";
    private volatile String lastStartedAt = "";
    private volatile int lastExitCode = -1;

    public CoreManagerService(AppRuntimeProperties appRuntimeProperties, ClashMetaProperties clashMetaProperties) {
        this.appRuntimeProperties = appRuntimeProperties;
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
        } else if (coreProcess != null && coreProcess.isAlive()) {
            effectiveState = "RUNNING";
        } else if ("RUNNING".equals(state)) {
            effectiveState = "EXITED";
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
            coreProcess = null;
            return getStatus();
        }

        if (!Files.exists(Path.of(configuredPath))) {
            state = "MISSING_BINARY";
            lastError = "Configured Clash.Meta binary does not exist";
            lastAction = "START";
            coreProcess = null;
            return getStatus();
        }

        if (coreProcess != null && coreProcess.isAlive()) {
            state = "RUNNING";
            lastAction = "START";
            lastError = "";
            return getStatus();
        }

        try {
            Path runtimeRoot = ensureRuntimeLayout();
            Path configPath = writeMinimalConfig(runtimeRoot);
            Path logPath = runtimeRoot.resolve("logs").resolve("clash-meta.log");

            ProcessBuilder processBuilder = new ProcessBuilder(
                    configuredPath,
                    "-f",
                    configPath.toAbsolutePath().toString(),
                    "-d",
                    runtimeRoot.toAbsolutePath().toString()
            );
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(logPath.toFile()));
            Process process = processBuilder.start();
            coreProcess = process;
            lastExitCode = -1;

            process.onExit().thenRun(() -> {
                if ("STOPPED".equals(state)) {
                    return;
                }

                state = "EXITED";
                lastExitCode = process.exitValue();
                lastError = "Clash.Meta process exited with code " + lastExitCode;
            });

            state = "RUNNING";
            lastError = "";
            lastAction = "START";
            lastStartedAt = OffsetDateTime.now().toString();
        } catch (IOException error) {
            state = "START_FAILED";
            lastAction = "START";
            lastError = error.getMessage();
        }

        return getStatus();
    }

    public CoreStatusResponse stop() {
        if (coreProcess != null && coreProcess.isAlive()) {
            coreProcess.destroy();
        }

        state = "STOPPED";
        lastAction = "STOP";
        lastError = "";
        return getStatus();
    }

    public CoreStatusResponse reload() {
        if (clashMetaProperties.getPath() == null || clashMetaProperties.getPath().isBlank()) {
            state = "NOT_CONFIGURED";
            lastAction = "RELOAD";
            lastError = "Clash.Meta path is not configured";
            return getStatus();
        }

        if (!Files.exists(Path.of(clashMetaProperties.getPath()))) {
            state = "MISSING_BINARY";
            lastAction = "RELOAD";
            lastError = "Configured Clash.Meta binary does not exist";
            return getStatus();
        }

        stop();
        lastAction = "RELOAD";
        return start();
    }

    private Path ensureRuntimeLayout() throws IOException {
        Path runtimeRoot = Path.of(appRuntimeProperties.getRoot(), "clash-meta");
        Files.createDirectories(runtimeRoot);
        Files.createDirectories(runtimeRoot.resolve("config"));
        Files.createDirectories(runtimeRoot.resolve("logs"));
        Files.createDirectories(runtimeRoot.resolve("state"));
        return runtimeRoot;
    }

    private Path writeMinimalConfig(Path runtimeRoot) throws IOException {
        Path configPath = runtimeRoot.resolve("config").resolve("config.yaml");
        List<String> lines = List.of(
                "mixed-port: " + MIXED_PORT,
                "allow-lan: false",
                "mode: rule",
                "log-level: info",
                "external-controller: 127.0.0.1:" + CONTROLLER_PORT,
                "log-file: " + runtimeRoot.resolve("logs").resolve("clash-meta.log").toAbsolutePath(),
                "proxies: []",
                "proxy-groups: []",
                "rules:",
                "  - MATCH,DIRECT"
        );
        Files.writeString(
                configPath,
                String.join(System.lineSeparator(), lines) + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
        return configPath;
    }
}
