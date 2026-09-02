package com.ruchi.order_update_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "order-update")
public class ThrottleProperties {
    private int maxEventsPerSecond = 50;
    public int getMaxEventsPerSecond(){
        return maxEventsPerSecond;
    }

    public void setMaxEventsPerSecond(int maxEventsPerSecond){
        this.maxEventsPerSecond = maxEventsPerSecond;
    }
}
