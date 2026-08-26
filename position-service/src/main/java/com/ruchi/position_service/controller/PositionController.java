package com.ruchi.position_service.controller;

import com.ruchi.position_service.store.PositionStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PositionController {
    private final PositionStore positionStore;
    public PositionController(PositionStore positionStore){
        this.positionStore = positionStore;
    }

    @GetMapping("/position")
    public Map<String, Long>getPositions(){
        return positionStore.getPositions();
    }
}
