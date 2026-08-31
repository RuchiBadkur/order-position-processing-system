package com.ruchi.order_update_service.model;

public record OrderEvent(
        String eventId,
        String symbol,
        String transactionType,
        int quantity
) {
}
