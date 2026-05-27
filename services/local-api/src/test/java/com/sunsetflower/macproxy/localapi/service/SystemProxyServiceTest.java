package com.sunsetflower.macproxy.localapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunsetflower.macproxy.localapi.config.AppRuntimeProperties;
import com.sunsetflower.macproxy.localapi.service.dto.CoreStatusResponse;
import com.sunsetflower.macproxy.localapi.service.dto.SettingsResponse;
import com.sunsetflower.macproxy.localapi.service.dto.SystemProxyStatusResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SystemProxyServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void enableUsesCurrentDynamicCorePortAndSelectedScope() {
        var settingsService = mock(SettingsService.class);
        when(settingsService.getSettings()).thenReturn(
                new SettingsResponse(false, "SELECTED", "Wi-Fi", "", false, "INFO"),
                new SettingsResponse(false, "SELECTED", "Wi-Fi", "", false, "INFO"),
                new SettingsResponse(true, "SELECTED", "Wi-Fi", "", false, "INFO")
        );
        when(settingsService.updateSystemProxyPreferences("SELECTED", "Wi-Fi"))
                .thenReturn(new SettingsResponse(false, "SELECTED", "Wi-Fi", "", false, "INFO"));
        when(settingsService.updateSystemProxyEnabled(true))
                .thenReturn(new SettingsResponse(true, "SELECTED", "Wi-Fi", "", false, "INFO"));

        var coreManager = mock(CoreManagerService.class);
        when(coreManager.start()).thenReturn(new CoreStatusResponse(
                "RUNNING", "/tmp/clash-meta", true, 61337, 61338, "START", "", ""
        ));
        when(coreManager.getEffectiveMixedPort()).thenReturn(61337);

        var service = new FakeSystemProxyService(runtimeProperties(), coreManager, settingsService, tempDir);
        service.networkServiceListOutput = """
                An asterisk (*) denotes that a network service is disabled.
                Wi-Fi
                Tailscale
                """;
        service.networkServiceOrderOutput = """
                (1) Wi-Fi
                (Hardware Port: Wi-Fi, Device: en0)
                (2) Tailscale
                (Hardware Port: Tailscale, Device: utun3)
                """;
        service.scutilOutput = """
                en0: flags 0x1
                  reach : Reachable
                """;
        service.proxyStateByCommand.put(key("-getwebproxy", "Wi-Fi"), disabledProxy());
        service.proxyStateByCommand.put(key("-getsecurewebproxy", "Wi-Fi"), disabledProxy());
        service.proxyStateByCommand.put(key("-getsocksfirewallproxy", "Wi-Fi"), disabledProxy());

        SystemProxyStatusResponse result = service.update(true, "SELECTED", List.of("Wi-Fi"), false);

        assertEquals(61337, result.targetPort());
        assertEquals(List.of("Wi-Fi"), result.services());
        assertTrue(service.executedCommands.contains(List.of("-setwebproxy", "Wi-Fi", "127.0.0.1", "61337", "off")));
        assertTrue(service.executedCommands.contains(List.of("-setsecurewebproxy", "Wi-Fi", "127.0.0.1", "61337", "off")));
        assertTrue(service.executedCommands.contains(List.of("-setsocksfirewallproxy", "Wi-Fi", "127.0.0.1", "61337", "off")));
        assertTrue(service.snapshotFileExists());
    }

    @Test
    void disableRestoresSnapshotInsteadOfBlindlyTurningEverythingOff() {
        var settingsService = mock(SettingsService.class);
        when(settingsService.getSettings()).thenReturn(
                new SettingsResponse(true, "SELECTED", "Wi-Fi", "", false, "INFO"),
                new SettingsResponse(true, "SELECTED", "Wi-Fi", "", false, "INFO"),
                new SettingsResponse(false, "SELECTED", "Wi-Fi", "", false, "INFO")
        );
        when(settingsService.updateSystemProxyPreferences("SELECTED", "Wi-Fi"))
                .thenReturn(new SettingsResponse(true, "SELECTED", "Wi-Fi", "", false, "INFO"));
        when(settingsService.updateSystemProxyEnabled(false))
                .thenReturn(new SettingsResponse(false, "SELECTED", "Wi-Fi", "", false, "INFO"));

        var coreManager = mock(CoreManagerService.class);
        when(coreManager.start()).thenReturn(new CoreStatusResponse(
                "RUNNING", "/tmp/clash-meta", true, 62000, 62001, "START", "", ""
        ));
        when(coreManager.getEffectiveMixedPort()).thenReturn(62000);

        var service = new FakeSystemProxyService(runtimeProperties(), coreManager, settingsService, tempDir);
        service.networkServiceListOutput = """
                An asterisk (*) denotes that a network service is disabled.
                Wi-Fi
                """;
        service.networkServiceOrderOutput = """
                (1) Wi-Fi
                (Hardware Port: Wi-Fi, Device: en0)
                """;
        service.scutilOutput = """
                en0: flags 0x1
                  reach : Reachable
                """;
        service.proxyStateByCommand.put(key("-getwebproxy", "Wi-Fi"), enabledProxy("10.0.0.2", 8080));
        service.proxyStateByCommand.put(key("-getsecurewebproxy", "Wi-Fi"), enabledProxy("10.0.0.2", 8443));
        service.proxyStateByCommand.put(key("-getsocksfirewallproxy", "Wi-Fi"), enabledProxy("10.0.0.2", 1080));

        service.update(true, "SELECTED", List.of("Wi-Fi"), false);
        service.executedCommands.clear();

        service.update(false, "SELECTED", List.of("Wi-Fi"), false);

        assertTrue(service.executedCommands.contains(List.of("-setwebproxy", "Wi-Fi", "10.0.0.2", "8080", "off")));
        assertTrue(service.executedCommands.contains(List.of("-setwebproxystate", "Wi-Fi", "on")));
        assertTrue(service.executedCommands.contains(List.of("-setsecurewebproxy", "Wi-Fi", "10.0.0.2", "8443", "off")));
        assertTrue(service.executedCommands.contains(List.of("-setsecurewebproxystate", "Wi-Fi", "on")));
        assertTrue(service.executedCommands.contains(List.of("-setsocksfirewallproxy", "Wi-Fi", "10.0.0.2", "1080", "off")));
        assertTrue(service.executedCommands.contains(List.of("-setsocksfirewallproxystate", "Wi-Fi", "on")));
        assertFalse(service.snapshotFileExists());
    }

    private AppRuntimeProperties runtimeProperties() {
        AppRuntimeProperties properties = new AppRuntimeProperties();
        properties.setRoot(tempDir.toString());
        properties.normalize();
        return properties;
    }

    private static String key(String command, String service) {
        return command + "::" + service;
    }

    private static String disabledProxy() {
        return """
                Enabled: No
                Server:
                Port: 0
                Authenticated Proxy Enabled: 0
                """;
    }

    private static String enabledProxy(String host, int port) {
        return """
                Enabled: Yes
                Server: %s
                Port: %d
                Authenticated Proxy Enabled: 0
                """.formatted(host, port);
    }

    private static final class FakeSystemProxyService extends SystemProxyService {
        private final Path runtimeRoot;
        private final Map<String, String> proxyStateByCommand = new HashMap<>();
        private final List<List<String>> executedCommands = new ArrayList<>();
        private String networkServiceListOutput = "";
        private String networkServiceOrderOutput = "";
        private String scutilOutput = "";

        private FakeSystemProxyService(
                AppRuntimeProperties appRuntimeProperties,
                CoreManagerService coreManagerService,
                SettingsService settingsService,
                Path runtimeRoot
        ) {
            super(appRuntimeProperties, coreManagerService, settingsService, new ObjectMapper());
            this.runtimeRoot = runtimeRoot;
        }

        @Override
        protected String executeCommand(String... arguments) {
            if (arguments.length == 1 && "-listallnetworkservices".equals(arguments[0])) {
                return networkServiceListOutput;
            }
            if (arguments.length == 1 && "-listnetworkserviceorder".equals(arguments[0])) {
                return networkServiceOrderOutput;
            }
            if (arguments.length == 2) {
                String output = proxyStateByCommand.get(key(arguments[0], arguments[1]));
                if (output != null) {
                    return output;
                }
            }
            throw new IllegalStateException("Unhandled executeCommand arguments: " + List.of(arguments));
        }

        @Override
        protected String executeRawCommand(String binary, String... arguments) {
            if (arguments.length == 1 && "--nwi".equals(arguments[0])) {
                return scutilOutput;
            }
            throw new IllegalStateException("Unhandled executeRawCommand arguments: " + binary + " " + List.of(arguments));
        }

        @Override
        protected void runNetworkSetup(String service, String command, String... extraArguments) throws IOException {
            List<String> commandLine = new ArrayList<>();
            commandLine.add(command);
            commandLine.add(service);
            commandLine.addAll(List.of(extraArguments));
            executedCommands.add(commandLine);

            if (command.startsWith("-get")) {
                return;
            }

            Map<String, String> currentState = new LinkedHashMap<>(parseProxyInfo(
                    proxyStateByCommand.getOrDefault(key(mapGetCommand(command), service), disabledProxy())
            ));

            if (command.equals("-setwebproxy") || command.equals("-setsecurewebproxy") || command.equals("-setsocksfirewallproxy")) {
                currentState.put("Server", extraArguments[0]);
                currentState.put("Port", extraArguments[1]);
                proxyStateByCommand.put(key(mapGetCommand(command), service), formatProxyInfo(currentState));
                return;
            }

            if (command.equals("-setwebproxystate") || command.equals("-setsecurewebproxystate") || command.equals("-setsocksfirewallproxystate")) {
                currentState.put("Enabled", "on".equalsIgnoreCase(extraArguments[0]) ? "Yes" : "No");
                proxyStateByCommand.put(key(mapGetCommand(command), service), formatProxyInfo(currentState));
                return;
            }

            throw new IllegalStateException("Unhandled runNetworkSetup command: " + commandLine);
        }

        private boolean snapshotFileExists() {
            return java.nio.file.Files.exists(snapshotPath());
        }

        private String mapGetCommand(String command) {
            return switch (command) {
                case "-setwebproxy", "-setwebproxystate" -> "-getwebproxy";
                case "-setsecurewebproxy", "-setsecurewebproxystate" -> "-getsecurewebproxy";
                case "-setsocksfirewallproxy", "-setsocksfirewallproxystate" -> "-getsocksfirewallproxy";
                default -> throw new IllegalStateException("Unknown command mapping: " + command);
            };
        }

        private String formatProxyInfo(Map<String, String> values) {
            return """
                    Enabled: %s
                    Server: %s
                    Port: %s
                    Authenticated Proxy Enabled: 0
                    """.formatted(
                    values.getOrDefault("Enabled", "No"),
                    values.getOrDefault("Server", ""),
                    values.getOrDefault("Port", "0")
            );
        }
    }
}
