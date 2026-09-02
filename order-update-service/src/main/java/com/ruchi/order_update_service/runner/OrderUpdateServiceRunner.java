package com.ruchi.order_update_service.runner;

import com.ruchi.order_update_service.client.PositionServiceClient;
import com.ruchi.order_update_service.config.PositionServiceProperties;
import com.ruchi.order_update_service.processor.OrderUpdateProcessor;
import com.ruchi.order_update_service.reader.OrderCsvReader;
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

    @Value("${order-update.csv-path}")
    private String csvPath;

    public OrderUpdateServiceRunner(
            PositionServiceClient client
    ){
        this.client = client;
    }

    @Override
    public void run(String... args){
        OrderCsvReader reader = new OrderCsvReader(Path.of(csvPath));

        OrderEventValidator validator = new OrderEventValidator();

        OrderUpdateProcessor processor = new OrderUpdateProcessor(validator, client);

        processor.process(reader);
    }
}
