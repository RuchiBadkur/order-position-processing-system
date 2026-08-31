package com.ruchi.order_update_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "order")
public class OrderUpdateProperties {
    private String inputFile;
    public String getInputFile(){
        return inputFile;
    }
    public void setInputFile(String inputFile){
        this.inputFile = inputFile;
    }
}
