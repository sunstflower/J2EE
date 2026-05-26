package com.sunsetflower.macproxy.localapi;

import com.sunsetflower.macproxy.localapi.config.AppSessionProperties;
import com.sunsetflower.macproxy.localapi.config.ClashMetaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AppSessionProperties.class, ClashMetaProperties.class})
public class LocalApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocalApiApplication.class, args);
    }
}
