package com.ruchi.order_update_service.reader;

import com.ruchi.order_update_service.model.RawOrderRow;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderCsvReaderTest {
    @Test
    void shouldReadCsvRows() throws Exception{
        Path csvFile = Files.createTempFile(
                "orders", ".csv"
        );
        Files.writeString(
                csvFile,
                """
                       event_id,symbol,transaction_type,quantity
                       evt-001,RELIANCE,BUY,90
                       evt-002,TCS,SELL,75
                       """
        );
        OrderCsvReader reader = new OrderCsvReader(csvFile);

        Iterator<RawOrderRow> iterator = reader.iterator();

        assertEquals(
                new RawOrderRow(
                        "evt-001",
                        "RELIANCE",
                        "BUY",
                        "90"
                ),
                iterator.next()
        );

        assertEquals(
                new RawOrderRow(
                        "evt-002",
                        "TCS",
                        "SELL",
                        "75"
                ),
                iterator.next()
        );

        assertFalse(iterator.hasNext());
    }

    @Test
    void shouldHandleMalformedRowWithoutCrashing() throws Exception {

        Path csvFile = Files.createTempFile("orders", ".csv");

        Files.writeString(csvFile,
                """
                event_id,symbol,transaction_type,quantity
                evt-001,RELIANCE,BUY,90
                evt-002,TCS
                evt-003,INFY,SELL,50
                """);

        OrderCsvReader reader = new OrderCsvReader(csvFile);

        Iterator<RawOrderRow> iterator = reader.iterator();

        RawOrderRow first = iterator.next();
        assertEquals("evt-001", first.eventId());

        RawOrderRow malformed = iterator.next();
        assertEquals(
                "Expected 4 columns but found 2",
                malformed.structuralError()
        );

        RawOrderRow third = iterator.next();
        assertEquals("evt-003", third.eventId());
    }
}
