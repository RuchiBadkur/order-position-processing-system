package com.ruchi.order_update_service.model;

public record RawOrderRow(
        String eventId,
        String symbol,
        String transactionType,
        String quantity,
        String structuralError
) {
    public RawOrderRow(
            String eventId,
            String symbol,
            String transactionType,
            String quantity
    ){
        this(eventId, symbol, transactionType, quantity, null);
    }
}
