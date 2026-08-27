package com.ruchi.position_service.controller;

import com.ruchi.position_service.model.OrderEvent;
import com.ruchi.position_service.store.PositionStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PositionController.class)
@Import(PositionStore.class)
class PositionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PositionStore positionStore;

    @Test
    void getPositionShouldReturnEmptyPositionsInitially() throws Exception {

        mockMvc.perform(get("/position"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getPositionShouldReturnCurrentPositions() throws Exception {

        positionStore.apply(
                new OrderEvent(
                        "evt-001",
                        "RELIANCE",
                        "BUY",
                        90
                )
        );

        positionStore.apply(
                new OrderEvent(
                        "evt-002",
                        "TCS",
                        "SELL",
                        75
                )
        );

        mockMvc.perform(get("/position"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RELIANCE").value(90))
                .andExpect(jsonPath("$.TCS").value(-75));
    }
}