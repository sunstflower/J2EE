package com.sunsetflower.macproxy.localapi.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "app.runtime")
public class AppRuntimeProperties {

    private String root = "./.runtime";

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    @PostConstruct
    public void normalize() {
        if (root == null || root.isBlank()) {
            root = "./.runtime";
        }

        root = Path.of(root).toAbsolutePath().normalize().toString();
    }
}
