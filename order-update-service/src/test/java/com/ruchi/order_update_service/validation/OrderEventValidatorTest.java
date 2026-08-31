package com.ruchi.order_update_service.validation;

import com.ruchi.order_update_service.model.RawOrderRow;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderEventValidatorTest {
    private final OrderEventValidator validator = new OrderEventValidator();

    @Test
    void shouldAcceptValidBuyEvent(){
        RawOrderRow row = new RawOrderRow(
                "evt-001",
                "RELIANCE",
                "BUY",
                "90"
        );

        ValidationResult result = validator.validate(row);

        assertTrue(result.valid());
        assertNotNull(result.event());
        assertEquals(90, result.event().quantity());
    }

    @Test
    void shouldAcceptValidSellEvent() {

        RawOrderRow row = new RawOrderRow(
                "evt-002",
                "TCS",
                "SELL",
                "75"
        );

        ValidationResult result = validator.validate(row);

        assertTrue(result.valid());
        assertEquals("SELL", result.event().transactionType());
        assertEquals(75, result.event().quantity());
    }

    @Test
    void shouldRejectInvalidTransactionType() {

        RawOrderRow row = new RawOrderRow(
                "evt-003",
                "TCS",
                "HOLD",
                "75"
        );

        ValidationResult result = validator.validate(row);

        assertFalse(result.valid());
        assertEquals(
                "transaction_type must be exactly BUY or SELL",
                result.reason()
        );
    }

    @Test
    void shouldRejectZeroQuantity() {

        RawOrderRow row = new RawOrderRow(
                "evt-004",
                "TCS",
                "BUY",
                "0"
        );

        ValidationResult result = validator.validate(row);

        assertFalse(result.valid());
    }

    @Test
    void shouldRejectNegativeQuantity() {

        RawOrderRow row = new RawOrderRow(
                "evt-005",
                "TCS",
                "BUY",
                "-10"
        );

        ValidationResult result = validator.validate(row);

        assertFalse(result.valid());
    }

    @Test
    void shouldRejectNonIntegerQuantity() {

        RawOrderRow row = new RawOrderRow(
                "evt-006",
                "TCS",
                "BUY",
                "abc"
        );

        ValidationResult result = validator.validate(row);

        assertFalse(result.valid());
    }

    @Test
    void shouldRejectBlankQuantity() {

        RawOrderRow row = new RawOrderRow(
                "evt-007",
                "TCS",
                "BUY",
                ""
        );

        ValidationResult result = validator.validate(row);

        assertFalse(result.valid());
    }

    @Test
    void shouldRejectBlankEventId() {

        RawOrderRow row = new RawOrderRow(
                "",
                "TCS",
                "BUY",
                "75"
        );

        ValidationResult result = validator.validate(row);

        assertFalse(result.valid());
    }

    @Test
    void shouldRejectBlankSymbol() {

        RawOrderRow row = new RawOrderRow(
                "evt-008",
                "",
                "BUY",
                "75"
        );

        ValidationResult result = validator.validate(row);

        assertFalse(result.valid());


    }
    @Test
    void shouldRejectLowercaseTransactionType() {

        RawOrderRow row = new RawOrderRow(
                "evt-009",
                "RELIANCE",
                "buy",
                "90"
        );

        ValidationResult result = validator.validate(row);

        assertFalse(result.valid());
    }

    @Test
    void shouldRejectTransactionTypeWithWhitespace() {

        RawOrderRow row = new RawOrderRow(
                "evt-010",
                "RELIANCE",
                "SELL ",
                "90"
        );

        ValidationResult result = validator.validate(row);

        assertFalse(result.valid());
    }
    @Test
    void shouldRejectStructurallyInvalidRow() {

        RawOrderRow row = new RawOrderRow(
                "",
                "",
                "",
                "",
                "Expected 4 columns but found 2"
        );

        ValidationResult result = validator.validate(row);

        assertFalse(result.valid());
        assertEquals(
                "Expected 4 columns but found 2",
                result.reason()
        );
    }
}
