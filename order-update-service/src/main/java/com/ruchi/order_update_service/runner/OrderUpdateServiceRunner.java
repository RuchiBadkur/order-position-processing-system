package com.ruchi.order_update_service.runner;

import com.ruchi.order_update_service.client.PositionServiceClient;
import com.ruchi.order_update_service.config.PositionServiceProperties;
import com.ruchi.order_update_service.config.ThrottleProperties;
import com.ruchi.order_update_service.processor.OrderUpdateProcessor;
import com.ruchi.order_update_service.reader.OrderCsvReader;
import com.ruchi.order_update_service.throttle.RateLimiter;
import com.ruchi.order_update_service.validation.OrderEventValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@ConditionalOnProperty(
        name = "order-update.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OrderUpdateServiceRunner implements CommandLineRunner {

    private final PositionServiceClient client;
    private final ThrottleProperties throttleProperties;

    @Value("${order-update.csv-path}")
    private String csvPath;

    public OrderUpdateServiceRunner(
            PositionServiceClient client,
            ThrottleProperties throttleProperties
    ){
        this.client = client;
        this.throttleProperties = throttleProperties;
    }

    @Override
    public void run(String... args){
        System.out.println("Starting csv processing: " + csvPath);
        OrderCsvReader reader = new OrderCsvReader(Path.of(csvPath));

        OrderEventValidator validator = new OrderEventValidator();

        RateLimiter rateLimiter = new RateLimiter(
                throttleProperties.getMaxEventsPerSecond()
        );

        OrderUpdateProcessor processor = new OrderUpdateProcessor(
                validator, client, rateLimiter
        );

        processor.process(reader);

        System.out.println("CSV processing completed.");
    }
}
