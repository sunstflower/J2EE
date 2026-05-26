package com.sunsetflower.macproxy.localapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.session")
public class AppSessionProperties {

    private String token = "dev-session-token";

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
