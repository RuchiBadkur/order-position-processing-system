package com.ruchi.order_update_service.processor;

import com.ruchi.order_update_service.client.PositionServiceClient;
import com.ruchi.order_update_service.model.OrderEvent;
import com.ruchi.order_update_service.model.RawOrderRow;
import com.ruchi.order_update_service.throttle.RateLimiter;
import com.ruchi.order_update_service.validation.OrderEventValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

public class OrderUpdateProcessorTest {


    @Test
    void shouldSendValidEventToPositionService(){
        OrderEventValidator validator = new OrderEventValidator();
        PositionServiceClient client = mock(PositionServiceClient.class);
        RateLimiter rateLimiter = new RateLimiter(50);

        OrderUpdateProcessor processor = new OrderUpdateProcessor(
                validator, client, rateLimiter
        );

        RawOrderRow row = new RawOrderRow(
                "evt-001",
                "RELIANCE",
                "BUY",
                "90"
        );

        processor.process(List.of(row));

        verify(client).send(
                new OrderEvent(
                        "evt-001",
                        "RELIANCE",
                        "BUY",
                        90
                )
        );
    }

    @Test
    void shouldSkipInvalidEvent(){
        OrderEventValidator validator = new OrderEventValidator();
        PositionServiceClient client = mock(PositionServiceClient.class);
        RateLimiter rateLimiter = new RateLimiter(50);

        OrderUpdateProcessor processor = new OrderUpdateProcessor(
                validator, client, rateLimiter
        );

        RawOrderRow row = new RawOrderRow(
                "evt-002",
                "TCS",
                "HOLD",
                "75"
        );

        processor.process(List.of(row));
        verifyNoInteractions(client);
    }

    @Test
    void shouldIgnoreDuplicateEventId(){
        OrderEventValidator validator = new OrderEventValidator();
        PositionServiceClient client = mock(PositionServiceClient.class);
        RateLimiter rateLimiter = new RateLimiter(50);

        OrderUpdateProcessor processor = new OrderUpdateProcessor(
                validator, client, rateLimiter
        );

        RawOrderRow first = new RawOrderRow(
                "evt-003",
                "RELIANCE",
                "BUY",
                "90"
        );

        RawOrderRow duplicate = new RawOrderRow(
                "evt-003",
                "TCS",
                "SELL",
                "500"
        );

        processor.process(List.of(first, duplicate));

        verify(client, times(1)).send(
                new OrderEvent(
                        "evt-003",
                        "RELIANCE",
                        "BUY",
                        90
                )
        );

    }

    @Test
    void shouldContinueAfterInvalidRow() {

        OrderEventValidator validator = new OrderEventValidator();
        PositionServiceClient client = mock(PositionServiceClient.class);
        RateLimiter rateLimiter = new RateLimiter(50);

        OrderUpdateProcessor processor =
                new OrderUpdateProcessor(validator, client, rateLimiter);

        RawOrderRow invalid = new RawOrderRow(
                "evt-004",
                "TCS",
                "HOLD",
                "75"
        );

        RawOrderRow valid = new RawOrderRow(
                "evt-005",
                "INFY",
                "BUY",
                "100"
        );

        processor.process(List.of(invalid, valid));

        verify(client).send(
                new OrderEvent(
                        "evt-005",
                        "INFY",
                        "BUY",
                        100
                )
        );
    }

    @Test
    void shouldContinueProcessingWhenDeliveryFails() {

        OrderEvent firstEvent =
                new OrderEvent("1", "TCS", "BUY", 100);

        OrderEvent secondEvent =
                new OrderEvent("2", "INFY", "BUY", 200);

        PositionServiceClient client =
                mock(PositionServiceClient.class);

        doThrow(new RuntimeException("Position service unavailable"))
                .when(client)
                .send(firstEvent);

        OrderEventValidator validator =
                new OrderEventValidator();

        RateLimiter rateLimiter =
                new RateLimiter(50);

        OrderUpdateProcessor processor =
                new OrderUpdateProcessor(
                        validator,
                        client,
                        rateLimiter
                );

        RawOrderRow firstRow =
                new RawOrderRow(
                        "1",
                        "TCS",
                        "BUY",
                        "100"
                );

        RawOrderRow secondRow =
                new RawOrderRow(
                        "2",
                        "INFY",
                        "BUY",
                        "200"
                );

        processor.process(
                java.util.List.of(firstRow, secondRow)
        );

        verify(client).send(firstEvent);
        verify(client).send(secondEvent);
    }
}
