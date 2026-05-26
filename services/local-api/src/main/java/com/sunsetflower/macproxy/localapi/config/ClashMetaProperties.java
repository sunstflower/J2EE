package com.sunsetflower.macproxy.localapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.core.clash-meta")
public class ClashMetaProperties {

    private String path = "";

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
