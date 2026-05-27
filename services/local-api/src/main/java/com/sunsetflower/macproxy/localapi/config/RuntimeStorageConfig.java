package com.sunsetflower.macproxy.localapi.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class RuntimeStorageConfig {

    private final AppRuntimeProperties appRuntimeProperties;

    public RuntimeStorageConfig(AppRuntimeProperties appRuntimeProperties) {
        this.appRuntimeProperties = appRuntimeProperties;
    }

    @PostConstruct
    public void ensureRuntimeDirectories() throws IOException {
        Path runtimeRoot = Path.of(appRuntimeProperties.getRoot());
        Files.createDirectories(runtimeRoot);
        Files.createDirectories(runtimeRoot.resolve("local-api"));
    }
}
