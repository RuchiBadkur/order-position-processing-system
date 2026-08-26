package com.ruchi.position_service.model;

public record OrderEvent (
        String eventId,
        String symbol,
        String transactionType,
        int quantity
){
}
