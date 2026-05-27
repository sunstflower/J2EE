package com.sunsetflower.macproxy.localapi.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "app.core.clash-meta")
public class ClashMetaProperties {

    private String path = "";

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @PostConstruct
    public void normalize() {
        if (path == null || path.isBlank()) {
            path = "";
            return;
        }

        path = Path.of(path).toAbsolutePath().normalize().toString();
    }
}
