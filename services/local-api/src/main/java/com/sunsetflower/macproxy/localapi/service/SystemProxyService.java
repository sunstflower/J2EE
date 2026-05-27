package com.sunsetflower.macproxy.localapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunsetflower.macproxy.localapi.config.AppRuntimeProperties;
import com.sunsetflower.macproxy.localapi.service.dto.SystemProxyStatusResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SystemProxyService {

    private static final String MODE = "MACOS_NETWORKSETUP";
    private static final String CAPABILITY_AVAILABLE = "AVAILABLE";
    private static final String CAPABILITY_UNAVAILABLE = "UNAVAILABLE";
    private static final String TARGET_HOST = "127.0.0.1";
    private static final int TARGET_PORT = 7890;
    private static final List<String> PREFERRED_SERVICE_TOKENS = List.of(
            "wi-fi",
            "ethernet",
            "usb 10/100/1000",
            "thunderbolt ethernet"
    );
    private static final List<String> EXCLUDED_SERVICE_TOKENS = List.of(
            "tailscale",
            "surge",
            "bridge",
            "vpn",
            "utun",
            "loopback",
            "bluetooth pan"
    );

    private final AppRuntimeProperties appRuntimeProperties;
    private final CoreManagerService coreManagerService;
    private final SettingsService settingsService;
    private final ObjectMapper objectMapper;

    private volatile String lastAction = "NONE";
    private volatile String lastError = "";

    public SystemProxyService(
            AppRuntimeProperties appRuntimeProperties,
            CoreManagerService coreManagerService,
            SettingsService settingsService,
            ObjectMapper objectMapper
    ) {
        this.appRuntimeProperties = appRuntimeProperties;
        this.coreManagerService = coreManagerService;
        this.settingsService = settingsService;
        this.objectMapper = objectMapper;
    }

    public SystemProxyStatusResponse getStatus() {
        var settings = settingsService.getSettings();
        boolean desiredEnabled = settings.systemProxyEnabled();
        List<String> availableServices = listEnabledNetworkServices();
        List<String> orderedServices = listNetworkServiceOrder();
        Map<String, String> serviceDevices = mapServiceToDevice(orderedServices);
        List<String> activeServices = detectActiveServices(availableServices, orderedServices, serviceDevices);
        List<String> recommendedServices = recommendServices(availableServices, orderedServices, activeServices);
        List<String> confirmedServices = parseSelectedServices(settings.systemProxyConfirmedServices());
        List<String> targetServices = resolveTargetServices(
                availableServices,
                recommendedServices,
                settings.systemProxyScope(),
                settings.systemProxyServices()
        );
        String capability = resolveCapability(availableServices, targetServices);
        boolean applied = CAPABILITY_AVAILABLE.equals(capability) && proxiesMatchTarget(targetServices);
        boolean recommendationPending = isRecommendationPending(
                settings.systemProxyScope(),
                confirmedServices,
                recommendedServices
        );

        return new SystemProxyStatusResponse(
                desiredEnabled,
                applied == desiredEnabled,
                MODE,
                resolveStatusLabel(desiredEnabled, applied, capability),
                capability,
                settings.systemProxyScope(),
                parseSelectedServices(settings.systemProxyServices()),
                confirmedServices,
                recommendedServices,
                availableServices,
                activeServices,
                recommendationPending,
                TARGET_HOST,
                TARGET_PORT,
                targetServices.size(),
                targetServices,
                lastAction,
                lastError
        );
    }

    public SystemProxyStatusResponse update(boolean enabled, String scope, List<String> services, boolean acceptRecommendedServices) {
        lastAction = enabled ? "ENABLE" : "DISABLE";

        try {
            settingsService.updateSystemProxyPreferences(scope, joinSelectedServices(services));

            var settings = settingsService.getSettings();
            List<String> availableServices = listEnabledNetworkServices();
            List<String> orderedServices = listNetworkServiceOrder();
            Map<String, String> serviceDevices = mapServiceToDevice(orderedServices);
            List<String> activeServices = detectActiveServices(availableServices, orderedServices, serviceDevices);
            List<String> recommendedServices = recommendServices(availableServices, orderedServices, activeServices);

            if (acceptRecommendedServices) {
                settingsService.updateSystemProxyConfirmedServices(joinSelectedServices(recommendedServices));
            }

            settings = settingsService.getSettings();
            List<String> targetServices = resolveTargetServices(
                    availableServices,
                    recommendedServices,
                    settings.systemProxyScope(),
                    settings.systemProxyServices()
            );
            String capability = resolveCapability(availableServices, targetServices);

            if (!CAPABILITY_AVAILABLE.equals(capability)) {
                throw new IllegalStateException("networksetup is unavailable or no eligible network services were found");
            }

            if (enabled) {
                ensureCoreRunning();
                writeSnapshot(targetServices);
                applyEnabledState(targetServices);
            } else {
                restoreOrDisable(targetServices);
            }

            settingsService.updateSystemProxyEnabled(enabled);
            lastError = "";
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            lastError = error.getMessage();
        } catch (IOException error) {
            lastError = error.getMessage();
        } catch (RuntimeException error) {
            lastError = error.getMessage();
        }

        return getStatus();
    }

    private void ensureCoreRunning() {
        var coreStatus = coreManagerService.start();
        if (!"RUNNING".equals(coreStatus.state())) {
            throw new IllegalStateException("Cannot enable system proxy because Clash.Meta is not running");
        }
    }

    private void applyEnabledState(List<String> services) throws IOException, InterruptedException {
        for (String service : services) {
            runNetworkSetup(service, "-setwebproxy", TARGET_HOST, String.valueOf(TARGET_PORT), "off");
            runNetworkSetup(service, "-setsecurewebproxy", TARGET_HOST, String.valueOf(TARGET_PORT), "off");
            runNetworkSetup(service, "-setsocksfirewallproxy", TARGET_HOST, String.valueOf(TARGET_PORT), "off");
            runNetworkSetup(service, "-setwebproxystate", "on");
            runNetworkSetup(service, "-setsecurewebproxystate", "on");
            runNetworkSetup(service, "-setsocksfirewallproxystate", "on");
        }
    }

    private void restoreOrDisable(List<String> services) throws IOException, InterruptedException {
        Snapshot snapshot = readSnapshot();

        if (snapshot == null) {
            disableProxyStates(services);
            return;
        }

        for (String service : services) {
            ServiceSnapshot serviceSnapshot = snapshot.services().get(service);
            if (serviceSnapshot == null) {
                disableProxyStates(List.of(service));
                continue;
            }

            restoreProtocolState(service, "-setwebproxy", "-setwebproxystate", serviceSnapshot.web());
            restoreProtocolState(service, "-setsecurewebproxy", "-setsecurewebproxystate", serviceSnapshot.secureWeb());
            restoreProtocolState(service, "-setsocksfirewallproxy", "-setsocksfirewallproxystate", serviceSnapshot.socks());
        }

        Files.deleteIfExists(snapshotPath());
    }

    private void restoreProtocolState(
            String service,
            String setCommand,
            String stateCommand,
            ProxyProtocolSnapshot snapshot
    ) throws IOException, InterruptedException {
        if (snapshot.server() != null && !snapshot.server().isBlank() && snapshot.port() > 0) {
            runNetworkSetup(service, setCommand, snapshot.server(), String.valueOf(snapshot.port()), "off");
        }

        runNetworkSetup(service, stateCommand, snapshot.enabled() ? "on" : "off");
    }

    private void disableProxyStates(List<String> services) throws IOException, InterruptedException {
        for (String service : services) {
            runNetworkSetup(service, "-setwebproxystate", "off");
            runNetworkSetup(service, "-setsecurewebproxystate", "off");
            runNetworkSetup(service, "-setsocksfirewallproxystate", "off");
        }
    }

    private boolean proxiesMatchTarget(List<String> services) {
        if (services.isEmpty()) {
            return false;
        }

        for (String service : services) {
            if (!protocolMatchesTarget(service, "-getwebproxy")) {
                return false;
            }
            if (!protocolMatchesTarget(service, "-getsecurewebproxy")) {
                return false;
            }
            if (!protocolMatchesTarget(service, "-getsocksfirewallproxy")) {
                return false;
            }
        }

        return true;
    }

    private boolean protocolMatchesTarget(String service, String command) {
        Map<String, String> values = parseProxyInfo(executeCommand(command, service));
        return "Yes".equalsIgnoreCase(values.getOrDefault("Enabled", "No"))
                && TARGET_HOST.equals(values.getOrDefault("Server", ""))
                && String.valueOf(TARGET_PORT).equals(values.getOrDefault("Port", ""));
    }

    private void writeSnapshot(List<String> services) throws IOException {
        Map<String, ServiceSnapshot> snapshots = new LinkedHashMap<>();
        for (String service : services) {
            snapshots.put(
                    service,
                    new ServiceSnapshot(
                            readProtocolSnapshot(service, "-getwebproxy"),
                            readProtocolSnapshot(service, "-getsecurewebproxy"),
                            readProtocolSnapshot(service, "-getsocksfirewallproxy")
                    )
            );
        }

        Path snapshotPath = snapshotPath();
        Files.createDirectories(snapshotPath.getParent());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(snapshotPath.toFile(), new Snapshot(snapshots));
    }

    private Snapshot readSnapshot() throws IOException {
        Path snapshotPath = snapshotPath();
        if (!Files.exists(snapshotPath)) {
            return null;
        }

        return objectMapper.readValue(snapshotPath.toFile(), Snapshot.class);
    }

    private ProxyProtocolSnapshot readProtocolSnapshot(String service, String command) {
        Map<String, String> values = parseProxyInfo(executeCommand(command, service));
        return new ProxyProtocolSnapshot(
                "Yes".equalsIgnoreCase(values.getOrDefault("Enabled", "No")),
                values.getOrDefault("Server", ""),
                parsePort(values.getOrDefault("Port", "0"))
        );
    }

    private int parsePort(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private List<String> listEnabledNetworkServices() {
        List<String> services = new ArrayList<>();
        for (String line : executeCommand("-listallnetworkservices").split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isBlank() || trimmed.startsWith("An asterisk")) {
                continue;
            }
            if (trimmed.startsWith("*")) {
                continue;
            }
            services.add(trimmed);
        }
        return services;
    }

    private boolean isRecommendationPending(
            String scope,
            List<String> confirmedServices,
            List<String> recommendedServices
    ) {
        if (!"SELECTED".equalsIgnoreCase(scope)) {
            return false;
        }

        return !joinSelectedServices(confirmedServices).equals(joinSelectedServices(recommendedServices));
    }

    private List<String> listNetworkServiceOrder() {
        List<String> orderedServices = new ArrayList<>();
        for (String line : executeCommand("-listnetworkserviceorder").split("\\R")) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("(") || !trimmed.contains(")")) {
                continue;
            }

            int closingIndex = trimmed.indexOf(')');
            if (closingIndex < 0 || closingIndex + 1 >= trimmed.length()) {
                continue;
            }

            String serviceName = trimmed.substring(closingIndex + 1).trim();
            if (!serviceName.isBlank()) {
                orderedServices.add(serviceName);
            }
        }
        return orderedServices;
    }

    private Map<String, String> mapServiceToDevice(List<String> orderedServices) {
        Map<String, String> serviceDevices = new HashMap<>();
        String currentService = null;

        for (String line : executeCommand("-listnetworkserviceorder").split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("(") && trimmed.contains(")")) {
                int closingIndex = trimmed.indexOf(')');
                currentService = closingIndex >= 0 ? trimmed.substring(closingIndex + 1).trim() : null;
                continue;
            }

            if (currentService == null || !trimmed.startsWith("(Hardware Port:")) {
                continue;
            }

            int deviceIndex = trimmed.indexOf("Device:");
            int closingParen = trimmed.lastIndexOf(')');
            if (deviceIndex < 0 || closingParen < deviceIndex) {
                continue;
            }

            String device = trimmed.substring(deviceIndex + "Device:".length(), closingParen).trim();
            if (!device.isBlank() && orderedServices.contains(currentService)) {
                serviceDevices.put(currentService, device);
            }
        }

        return serviceDevices;
    }

    private List<String> detectActiveServices(
            List<String> availableServices,
            List<String> orderedServices,
            Map<String, String> serviceDevices
    ) {
        List<String> activeDevices = listActiveDevices();
        if (activeDevices.isEmpty()) {
            return List.of();
        }

        List<String> activeServices = new ArrayList<>();
        for (String service : sortByServiceOrder(availableServices, orderedServices)) {
            String device = serviceDevices.get(service);
            if (device != null && activeDevices.contains(device) && !isExcludedService(service)) {
                activeServices.add(service);
            }
        }
        return activeServices;
    }

    private List<String> listActiveDevices() {
        List<String> activeDevices = new ArrayList<>();
        String currentDevice = null;

        for (String line : executeCommand("scutil", "--nwi").split("\\R")) {
            String rawLine = line.stripTrailing();
            String trimmed = rawLine.trim();
            if (trimmed.isBlank()) {
                continue;
            }

            if (!rawLine.startsWith(" ") && trimmed.contains(": flags")) {
                currentDevice = trimmed.substring(0, trimmed.indexOf(':')).trim();
                continue;
            }

            if (currentDevice == null || !trimmed.startsWith("reach")) {
                continue;
            }

            if (trimmed.contains("Reachable") && !currentDevice.startsWith("utun")) {
                activeDevices.add(currentDevice);
            }
        }

        return activeDevices.stream().distinct().toList();
    }

    private List<String> resolveTargetServices(
            List<String> availableServices,
            List<String> recommendedServices,
            String scope,
            String configuredServices
    ) {
        if ("SELECTED".equalsIgnoreCase(scope)) {
            Set<String> selected = new LinkedHashSet<>(parseSelectedServices(configuredServices));
            List<String> resolved = availableServices.stream()
                    .filter(selected::contains)
                    .toList();
            return resolved.isEmpty() ? recommendedServices : resolved;
        }

        return availableServices;
    }

    private List<String> recommendServices(
            List<String> availableServices,
            List<String> orderedServices,
            List<String> activeServices
    ) {
        List<String> activePreferred = activeServices.stream()
                .filter(this::isPreferredService)
                .toList();

        if (!activePreferred.isEmpty()) {
            return sortByServiceOrder(activePreferred, orderedServices);
        }

        if (!activeServices.isEmpty()) {
            return sortByServiceOrder(activeServices, orderedServices);
        }

        List<String> preferred = availableServices.stream()
                .filter(this::isPreferredService)
                .toList();

        if (!preferred.isEmpty()) {
            return sortByServiceOrder(preferred, orderedServices);
        }

        List<String> filtered = availableServices.stream()
                .filter(service -> !isExcludedService(service))
                .toList();

        List<String> fallback = filtered.isEmpty() ? availableServices : filtered;
        return sortByServiceOrder(fallback, orderedServices);
    }

    private boolean isPreferredService(String serviceName) {
        String normalized = serviceName.toLowerCase();
        if (isExcludedService(serviceName)) {
            return false;
        }

        return PREFERRED_SERVICE_TOKENS.stream().anyMatch(normalized::contains);
    }

    private boolean isExcludedService(String serviceName) {
        String normalized = serviceName.toLowerCase();
        return EXCLUDED_SERVICE_TOKENS.stream().anyMatch(normalized::contains);
    }

    private List<String> sortByServiceOrder(List<String> services, List<String> orderedServices) {
        Map<String, Integer> orderIndex = new LinkedHashMap<>();
        for (int index = 0; index < orderedServices.size(); index++) {
            orderIndex.put(orderedServices.get(index), index);
        }

        return services.stream()
                .sorted((left, right) -> {
                    int leftIndex = orderIndex.getOrDefault(left, Integer.MAX_VALUE);
                    int rightIndex = orderIndex.getOrDefault(right, Integer.MAX_VALUE);
                    if (leftIndex != rightIndex) {
                        return Integer.compare(leftIndex, rightIndex);
                    }
                    return left.compareToIgnoreCase(right);
                })
                .toList();
    }

    private List<String> parseSelectedServices(String configuredServices) {
        if (configuredServices == null || configuredServices.isBlank()) {
            return List.of();
        }

        return Arrays.stream(configuredServices.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    private String joinSelectedServices(List<String> services) {
        if (services == null || services.isEmpty()) {
            return "";
        }

        return services.stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .collect(Collectors.joining(","));
    }

    private String resolveCapability(List<String> availableServices, List<String> targetServices) {
        if (!isMacOs() || !networkSetupExists() || availableServices.isEmpty() || targetServices.isEmpty()) {
            return CAPABILITY_UNAVAILABLE;
        }
        return CAPABILITY_AVAILABLE;
    }

    private String resolveStatusLabel(boolean desiredEnabled, boolean applied, String capability) {
        if (!CAPABILITY_AVAILABLE.equals(capability)) {
            return desiredEnabled ? "Unavailable" : "Unavailable";
        }

        if (desiredEnabled && applied) {
            return "Applied On";
        }
        if (!desiredEnabled && !applied) {
            return "Applied Off";
        }
        if (desiredEnabled) {
            return "Pending On";
        }
        return "Pending Off";
    }

    private boolean isMacOs() {
        return System.getProperty("os.name", "").toLowerCase().contains("mac");
    }

    private boolean networkSetupExists() {
        return Files.exists(Path.of(resolveNetworkSetupBinary()));
    }

    private String resolveNetworkSetupBinary() {
        String explicit = "/usr/sbin/networksetup";
        return Files.exists(Path.of(explicit)) ? explicit : "networksetup";
    }

    private String executeCommand(String... arguments) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(buildCommand(arguments));
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IllegalStateException(output.isBlank() ? "networksetup command failed" : output.trim());
            }
            return output;
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(error.getMessage(), error);
        } catch (IOException error) {
            throw new IllegalStateException(error.getMessage(), error);
        }
    }

    private void runNetworkSetup(String service, String command, String... extraArguments) throws IOException, InterruptedException {
        List<String> arguments = new ArrayList<>();
        arguments.add(command);
        arguments.add(service);
        arguments.addAll(List.of(extraArguments));

        ProcessBuilder processBuilder = new ProcessBuilder(buildCommand(arguments.toArray(String[]::new)));
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException(output.isBlank() ? "networksetup command failed" : output.trim());
        }
    }

    private List<String> buildCommand(String... arguments) {
        List<String> command = new ArrayList<>();
        command.add(resolveNetworkSetupBinary());
        command.addAll(List.of(arguments));
        return command;
    }

    private Map<String, String> parseProxyInfo(String output) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String line : output.split("\\R")) {
            int separator = line.indexOf(':');
            if (separator <= 0) {
                continue;
            }
            String key = line.substring(0, separator).trim();
            String value = line.substring(separator + 1).trim();
            values.put(key, value);
        }
        return values;
    }

    private Path snapshotPath() {
        return Path.of(appRuntimeProperties.getRoot(), "system-proxy", "state", "snapshot.json");
    }

    private record Snapshot(Map<String, ServiceSnapshot> services) {
    }

    private record ServiceSnapshot(
            ProxyProtocolSnapshot web,
            ProxyProtocolSnapshot secureWeb,
            ProxyProtocolSnapshot socks
    ) {
    }

    private record ProxyProtocolSnapshot(
            boolean enabled,
            String server,
            int port
    ) {
    }
}
