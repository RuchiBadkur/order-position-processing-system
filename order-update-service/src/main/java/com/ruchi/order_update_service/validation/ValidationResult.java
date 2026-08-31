package com.ruchi.order_update_service.validation;

import com.ruchi.order_update_service.model.OrderEvent;

public record ValidationResult(
        boolean valid,
        OrderEvent event,
        String reason
) {
    public static ValidationResult valid(
            OrderEvent event
    ){
        return new ValidationResult(
                true, event, null
        );
    }

    public static ValidationResult invalid(
            String reason
    ){
        return  new ValidationResult(
                false, null, reason
        );
    }
}
