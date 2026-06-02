package com.example.drugmanagement.common.config;

import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisConfig {

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            configuration.setMapUnderscoreToCamelCase(true);
            configuration.setJdbcTypeForNull(org.apache.ibatis.type.JdbcType.NULL);
        };
    }
}
