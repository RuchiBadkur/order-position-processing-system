package com.ruchi.order_update_service.client;

import com.ruchi.order_update_service.config.PositionServiceProperties;
import com.ruchi.order_update_service.model.OrderEvent;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class PositionServiceClientTest {
    @Test
    void shouldSendOrderEventToPositionService() throws IOException {

        AtomicReference<String> receiveBody = new AtomicReference<>();
        AtomicReference<String> receivedMethod = new AtomicReference<>();

        HttpServer server = HttpServer.create(
                new InetSocketAddress(0),
                0
        );

        server.createContext("/events", exchange -> {
            receivedMethod.set(exchange.getRequestMethod());
            String body = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            receiveBody.set(body);

            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });

        server.start();

        try {
            int port = server.getAddress().getPort();
            RestClient restClient = RestClient.builder().build();

            PositionServiceProperties properties = new PositionServiceProperties();
            properties.setUrl("http://localhost:" + port);

            PositionServiceClient client = new PositionServiceClient(
                    restClient,
                    properties
            );

            OrderEvent event = new OrderEvent(
                    "evt-001",
                    "RELIANCE",
                    "BUY",
                    90
            );

            client.send(event);

            assertEquals("POST", receivedMethod.get());

            String body = receiveBody.get();

            assertTrue(body.contains("\"eventId\":\"evt-001\""));
            assertTrue(body.contains("\"symbol\":\"RELIANCE\""));
            assertTrue(body.contains("\"transactionType\":\"BUY\""));
            assertTrue(body.contains("\"quantity\":90"));
        } finally {
            server.stop(0);
        }
    }
}
