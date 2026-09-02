package com.ruchi.order_update_service.processor;

import com.ruchi.order_update_service.client.PositionServiceClient;
import com.ruchi.order_update_service.model.OrderEvent;
import com.ruchi.order_update_service.model.RawOrderRow;
import com.ruchi.order_update_service.validation.OrderEventValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

public class OrderUpdateProcessorTest {


    @Test
    void shouldSendValidEventToPositionService(){
        OrderEventValidator validator = new OrderEventValidator();
        PositionServiceClient client = mock(PositionServiceClient.class);

        OrderUpdateProcessor processor = new OrderUpdateProcessor(validator, client);

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

        OrderUpdateProcessor processor = new OrderUpdateProcessor(
                validator, client
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

        OrderUpdateProcessor processor = new OrderUpdateProcessor(
                validator, client
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

        OrderUpdateProcessor processor =
                new OrderUpdateProcessor(validator, client);

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
}
