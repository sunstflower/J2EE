package com.sunsetflower.macproxy.localapi.bootstrap;

import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@Component
public class LocalApiReadyLogger {

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        if (event.getApplicationContext() instanceof WebServerApplicationContext webContext) {
            int port = webContext.getWebServer().getPort();
            System.out.println("LOCAL_API_READY port=" + port);
        }
    }
}
