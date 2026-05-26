package com.sunsetflower.macproxy.localapi.config;

import com.sunsetflower.macproxy.localapi.web.SessionTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SessionTokenInterceptor sessionTokenInterceptor;

    public WebConfig(SessionTokenInterceptor sessionTokenInterceptor) {
        this.sessionTokenInterceptor = sessionTokenInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionTokenInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns("/api/v1/health");
    }
}
