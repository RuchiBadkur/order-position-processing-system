package com.ruchi.order_update_service.validation;

import com.ruchi.order_update_service.model.OrderEvent;
import com.ruchi.order_update_service.model.RawOrderRow;
import org.springframework.stereotype.Component;

@Component
public class OrderEventValidator {
    public ValidationResult validate(RawOrderRow row){
        if(row.structuralError() != null){
            return ValidationResult.invalid(row.structuralError());
        }

        if(row.eventId() == null || row.eventId().isBlank()){
            return ValidationResult.invalid("symbol must not be blank");
        }

        if(row.symbol() == null || row.symbol().isBlank()){
            return ValidationResult.invalid("Symbol must not be blank");
        }

        if(!"BUY".equals(row.transactionType())
            && !"SELL".equals(row.transactionType())){
            return ValidationResult.invalid(
                    "transaction_type must be exactly BUY or SELL"
            );
        }

        if(row.quantity() == null || row.quantity().isBlank()){
            return ValidationResult.invalid("quantity must not be blank");
        }

        int quantity;

        try{
            quantity = Integer.parseInt(row.quantity());
        }catch (NumberFormatException e){
            return ValidationResult.invalid(
                    "quantity must be a positive integer"
            );
        }

        if(quantity <= 0){
            return ValidationResult.invalid(
                    "quantity must be a positive integer"
            );
        }

        OrderEvent event = new OrderEvent(
                row.eventId(),
                row.symbol(),
                row.transactionType(),
                quantity
        );
        return ValidationResult.valid(event);
    }
}
