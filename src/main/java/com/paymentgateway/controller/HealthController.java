// src/main/java/com/paymentgateway/controller/HealthController.java
package com.paymentgateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
    
    @GetMapping("/")
    public String home() {
        return "Payment Gateway API is running!";
    }
}