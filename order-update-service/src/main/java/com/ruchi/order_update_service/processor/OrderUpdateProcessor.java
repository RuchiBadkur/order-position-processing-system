package com.ruchi.order_update_service.processor;

import com.ruchi.order_update_service.client.PositionServiceClient;
import com.ruchi.order_update_service.model.OrderEvent;
import com.ruchi.order_update_service.model.RawOrderRow;
import com.ruchi.order_update_service.throttle.RateLimiter;
import com.ruchi.order_update_service.validation.OrderEventValidator;
import com.ruchi.order_update_service.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class OrderUpdateProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(OrderUpdateProcessor.class);

    private final OrderEventValidator validator;
    private final PositionServiceClient client;
    private final RateLimiter rateLimiter;

    private final Set<String> processedEventIds = new HashSet<>();

    public OrderUpdateProcessor(
            OrderEventValidator validator,
            PositionServiceClient client,
            RateLimiter rateLimiter
    ){
        this.validator = validator;
        this.client = client;
        this.rateLimiter = rateLimiter;
    }

    public void process(Iterable<RawOrderRow> rows){
        for (RawOrderRow row : rows){
            ValidationResult result = validator.validate(row);

            if (!result.valid()){
                log.warn(
                        "Rejected event: eventId={}, symbol={}, reason={}",
                        row.eventId(),
                        row.symbol(),
                        result.reason()
                );
                continue;
            }

            OrderEvent event = result.event();

            if (processedEventIds.contains(event.eventId())){
                log.warn(
                        "Duplicate event ignored: eventId={}",
                        event.eventId()
                );
                continue;
            }

            log.info(
                  "Accepted event: eventId={}, symbol={}, type={}, quantity={}",
                  event.eventId(),
                  event.symbol(),
                  event.transactionType(),
                  event.quantity()
            );

            try {
                rateLimiter.acquire();

                client.send(event);

                processedEventIds.add(event.eventId());

                log.info(
                        "Successfully sent event: eventId={}",
                        event.eventId()
                );

            } catch (Exception e) {
                log.error(
                        "Failed to send event: eventId={}, reason={}",
                        event.eventId(),
                        e.getMessage()
                );
            }
        }

        log.info("Input processing complete");
    }
}
