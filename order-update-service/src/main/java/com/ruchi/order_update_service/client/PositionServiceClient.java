package com.ruchi.order_update_service.client;

import com.ruchi.order_update_service.config.PositionServiceProperties;
import com.ruchi.order_update_service.model.OrderEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PositionServiceClient {
    private final RestClient restClient;
    private final PositionServiceProperties properties;

    public PositionServiceClient(
            RestClient restClient,
            PositionServiceProperties properties
    ){
        this.restClient = restClient;
        this.properties = properties;
    }

    public void send(OrderEvent event){
        restClient.post()
                .uri(properties.getUrl() + "/events")
                .body(event)
                .retrieve()
                .toBodilessEntity();
    }
}
