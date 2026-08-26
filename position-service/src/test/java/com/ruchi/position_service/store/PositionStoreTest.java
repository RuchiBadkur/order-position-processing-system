package com.ruchi.position_service.store;

import com.ruchi.position_service.model.OrderEvent;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PositionStoreTest {
    @Test
    void buyShouldIncreasePosition(){
        PositionStore store = new PositionStore();
        store.apply(new OrderEvent(
                "evt-001",
                "RELIANCE",
                "BUY",
                90
        ));
        assertEquals(90L, store.getPositions().get("RELIANCE"));
    }

    @Test
    void sellShouldDecreasePosition(){
        PositionStore store = new PositionStore();
        store.apply(new OrderEvent(
                "evt-001",
                "RELIANCE",
                "SELL",
                75
                ));

        assertEquals(-75L, store.getPositions().get("RELIANCE"));
    }

    @Test
    void shouldMaintainPositionsForMultipleSymbols(){
        PositionStore store = new PositionStore();

        store.apply(new OrderEvent(
                "evt-001",
                "RELIANCE",
                "BUY",
                90
                ));
        store.apply(new OrderEvent(
                "evt-002",
                "TCS",
                "SELL",
                75
        ));

        Map<String, Long> positions = store.getPositions();

        assertEquals(90L, positions.get("RELIANCE"));
        assertEquals(-75L, positions.get("TCS"));

    }

    @Test
    void shouldAllowPositionToBecomeZero(){
        PositionStore store = new PositionStore();
        store.apply(new OrderEvent(
                "evt-001",
                "RELIANCE",
                "BUY",
                90
        ));
        store.apply(new OrderEvent(
                "evt-002",
                "RELIANCE",
                "SELL",
                90
        ));

        assertEquals(0L, store.getPositions().get("RELIANCE"));

    }

    @Test
    void shouldIgnoreDuplicateEventId(){
        PositionStore store = new PositionStore();
        store.apply(new OrderEvent(
                "evt-001", "RELIANCE", "BUY", 90
        ));
        store.apply(new OrderEvent(
                "evt-001", "RELIANCE", "BUY", 50
        ));

        assertEquals(90L, store.getPositions().get("RELIANCE"));
    }

}
