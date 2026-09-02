package com.ruchi.order_update_service.processor;

import com.ruchi.order_update_service.client.PositionServiceClient;
import com.ruchi.order_update_service.model.OrderEvent;
import com.ruchi.order_update_service.model.RawOrderRow;
import com.ruchi.order_update_service.validation.OrderEventValidator;
import com.ruchi.order_update_service.validation.ValidationResult;

import java.util.HashSet;
import java.util.Set;

public class OrderUpdateProcessor {
    private final OrderEventValidator validator;
    private final PositionServiceClient client;

    private final Set<String> processedEventIds = new HashSet<>();

    public OrderUpdateProcessor(
            OrderEventValidator validator,
            PositionServiceClient client
    ){
        this.validator = validator;
        this.client = client;
    }

    public void process(Iterable<RawOrderRow> rows){
        for (RawOrderRow row : rows){
            ValidationResult result = validator.validate(row);

            if (!result.valid()){
                continue;
            }

            OrderEvent event = result.event();

            if (processedEventIds.contains(event.eventId())){
                continue;
            }
            client.send(event);
            processedEventIds.add(event.eventId());
        }
    }
}
