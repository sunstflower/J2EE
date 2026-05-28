package com.sunsetflower.macproxy.localapi.service;

import com.sunsetflower.macproxy.localapi.config.AppRuntimeProperties;
import com.sunsetflower.macproxy.localapi.config.ClashMetaProperties;
import com.sunsetflower.macproxy.localapi.service.dto.CoreStatusResponse;
import com.sunsetflower.macproxy.localapi.service.dto.ImportedProxyNodeRecord;
import com.sunsetflower.macproxy.localapi.service.dto.ProxyGroupSelectionResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CoreManagerService {

    private static final String LOOPBACK_HOST = "127.0.0.1";
    private static final long START_STABILITY_WAIT_MILLIS = 400L;
    private static final long STOP_STABILITY_WAIT_MILLIS = 2_000L;

    private final AppRuntimeProperties appRuntimeProperties;
    private final ClashMetaProperties clashMetaProperties;
    private final ImportedProxyNodesService importedProxyNodesService;
    private final ProxyGroupsService proxyGroupsService;

    private volatile Process coreProcess;
    private volatile String state = "NOT_CONFIGURED";
    private volatile String lastError = "";
    private volatile String lastAction = "NONE";
    private volatile String lastStartedAt = "";
    private volatile int lastExitCode = -1;
    private volatile int mixedPort = 0;
    private volatile int controllerPort = 0;

    public CoreManagerService(
            AppRuntimeProperties appRuntimeProperties,
            ClashMetaProperties clashMetaProperties,
            ImportedProxyNodesService importedProxyNodesService,
            ProxyGroupsService proxyGroupsService
    ) {
        this.appRuntimeProperties = appRuntimeProperties;
        this.clashMetaProperties = clashMetaProperties;
        this.importedProxyNodesService = importedProxyNodesService;
        this.proxyGroupsService = proxyGroupsService;
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
        } else if ("NOT_CONFIGURED".equals(state) || "MISSING_BINARY".equals(state)) {
            effectiveState = "STOPPED";
        }

        return new CoreStatusResponse(
                effectiveState,
                configuredPath,
                exists,
                mixedPort,
                controllerPort,
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
            int nextMixedPort = allocateEphemeralPort();
            int nextControllerPort = allocateDistinctEphemeralPort(nextMixedPort);
            mixedPort = nextMixedPort;
            controllerPort = nextControllerPort;
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
                if ("STOPPED".equals(state) || coreProcess != process) {
                    return;
                }

                state = "EXITED";
                lastExitCode = process.exitValue();
                lastError = "Clash.Meta process exited with code " + lastExitCode;
            });

            verifyProcessStayedAlive(process, logPath);

            state = "RUNNING";
            lastError = "";
            lastAction = "START";
            lastStartedAt = OffsetDateTime.now().toString();
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            state = "START_FAILED";
            lastAction = "START";
            lastError = "Interrupted while waiting for Clash.Meta startup";
        } catch (IOException error) {
            state = "START_FAILED";
            lastAction = "START";
            lastError = error.getMessage();
        }

        return getStatus();
    }

    public CoreStatusResponse stop() {
        stopProcess();
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

        stopProcess();
        CoreStatusResponse status = start();
        lastAction = "RELOAD";
        return new CoreStatusResponse(
                status.state(),
                status.configuredPath(),
                status.binaryExists(),
                status.mixedPort(),
                status.controllerPort(),
                "RELOAD",
                status.lastStartedAt(),
                status.lastError()
        );
    }

    public int getEffectiveMixedPort() {
        return mixedPort;
    }

    public Path renderConfigForCurrentState() throws IOException {
        Path runtimeRoot = ensureRuntimeLayout();
        return writeMinimalConfig(runtimeRoot);
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
        List<String> lines = new ArrayList<>();
        lines.add("mixed-port: " + mixedPort);
        lines.add("allow-lan: false");
        lines.add("mode: rule");
        lines.add("log-level: info");
        lines.add("external-controller: " + LOOPBACK_HOST + ":" + controllerPort);
        lines.add("log-file: " + runtimeRoot.resolve("logs").resolve("clash-meta.log").toAbsolutePath());

        List<ImportedProxyNodeRecord> importedNodes = importedProxyNodesService.getAllNodes();
        appendProxies(lines, importedNodes);
        appendProxyGroups(lines, proxyGroupsService.getGroups());

        lines.add("rules:");
        lines.add("  - MATCH,DIRECT");
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

    private void appendProxies(List<String> lines, List<ImportedProxyNodeRecord> importedNodes) {
        if (importedNodes.isEmpty()) {
            lines.add("proxies: []");
            return;
        }

        lines.add("proxies:");
        for (ImportedProxyNodeRecord node : importedNodes) {
            lines.add("  - name: \"" + escapeYaml(node.nodeName()) + "\"");
            lines.add("    type: " + node.nodeType());
            lines.add("    server: " + node.server());
            lines.add("    port: " + node.port());
            lines.add("    udp: false");
            if ("ss".equalsIgnoreCase(node.nodeType())) {
                lines.add("    cipher: " + (node.cipher() == null || node.cipher().isBlank() ? "aes-128-gcm" : node.cipher()));
                lines.add("    password: \"" + escapeYaml(node.password() == null || node.password().isBlank() ? "placeholder-password" : node.password()) + "\"");
            } else if ("vmess".equalsIgnoreCase(node.nodeType())) {
                lines.add("    uuid: " + (node.uuid() == null || node.uuid().isBlank() ? "00000000-0000-0000-0000-000000000000" : node.uuid()));
                lines.add("    alterId: " + (node.alterId() == null ? 0 : node.alterId()));
                lines.add("    cipher: " + (node.cipher() == null || node.cipher().isBlank() ? "auto" : node.cipher()));
                lines.add("    tls: " + Boolean.TRUE.equals(node.tls()));
                if (node.network() != null && !node.network().isBlank()) {
                    lines.add("    network: " + node.network());
                }
                if (node.serverName() != null && !node.serverName().isBlank()) {
                    lines.add("    servername: \"" + escapeYaml(node.serverName()) + "\"");
                }
                if ((node.wsPath() != null && !node.wsPath().isBlank())
                        || (node.wsHost() != null && !node.wsHost().isBlank())) {
                    lines.add("    ws-opts:");
                    if (node.wsPath() != null && !node.wsPath().isBlank()) {
                        lines.add("      path: \"" + escapeYaml(node.wsPath()) + "\"");
                    }
                    if (node.wsHost() != null && !node.wsHost().isBlank()) {
                        lines.add("      headers:");
                        lines.add("        Host: \"" + escapeYaml(node.wsHost()) + "\"");
                    }
                }
            }
        }
    }

    private void appendProxyGroups(List<String> lines, List<ProxyGroupSelectionResponse> groups) {
        if (groups.isEmpty()) {
            lines.add("proxy-groups: []");
            return;
        }

        lines.add("proxy-groups:");
        for (ProxyGroupSelectionResponse group : groups) {
            lines.add("  - name: \"" + escapeYaml(group.groupName()) + "\"");
            lines.add("    type: select");
            lines.add("    proxies:");

            if (group.availableNodeNames().isEmpty()) {
                lines.add("      - DIRECT");
                continue;
            }

            String preferred = group.selectedNodeName();
            if (preferred != null && !preferred.isBlank() && group.availableNodeNames().contains(preferred)) {
                lines.add("      - \"" + escapeYaml(preferred) + "\"");
            }

            for (String nodeName : group.availableNodeNames()) {
                if (nodeName.equals(preferred)) {
                    continue;
                }
                lines.add("      - \"" + escapeYaml(nodeName) + "\"");
            }
        }
    }

    private String escapeYaml(String value) {
        return value.replace("\"", "\\\"");
    }

    private void verifyProcessStayedAlive(Process process, Path logPath) throws IOException, InterruptedException {
        Thread.sleep(START_STABILITY_WAIT_MILLIS);

        if (process.isAlive()) {
            return;
        }

        lastExitCode = process.exitValue();
        coreProcess = null;
        throw new IOException(buildEarlyExitMessage(logPath, lastExitCode));
    }

    private String buildEarlyExitMessage(Path logPath, int exitCode) throws IOException {
        String logTail = readLogTail(logPath);
        if (logTail.isBlank()) {
            return "Clash.Meta exited during startup with code " + exitCode;
        }

        return "Clash.Meta exited during startup with code " + exitCode + ": " + logTail;
    }

    private String readLogTail(Path logPath) throws IOException {
        if (!Files.exists(logPath)) {
            return "";
        }

        List<String> lines = Files.readAllLines(logPath, StandardCharsets.UTF_8);
        int fromIndex = Math.max(0, lines.size() - 6);
        return String.join(" | ", lines.subList(fromIndex, lines.size())).trim();
    }

    private void stopProcess() {
        Process process = coreProcess;
        coreProcess = null;

        if (process == null) {
            return;
        }

        if (!process.isAlive()) {
            lastExitCode = process.exitValue();
            return;
        }

        process.destroy();
        try {
            if (!process.waitFor(STOP_STABILITY_WAIT_MILLIS, TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
                process.waitFor(STOP_STABILITY_WAIT_MILLIS, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            lastError = "Interrupted while stopping Clash.Meta";
            return;
        }

        if (!process.isAlive()) {
            lastExitCode = process.exitValue();
        }
    }

    private int allocateEphemeralPort() throws IOException {
        try (ServerSocket socket = new ServerSocket()) {
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(LOOPBACK_HOST, 0));
            return socket.getLocalPort();
        }
    }

    private int allocateDistinctEphemeralPort(int occupiedPort) throws IOException {
        int candidate = allocateEphemeralPort();
        while (candidate == occupiedPort) {
            candidate = allocateEphemeralPort();
        }
        return candidate;
    }
}
