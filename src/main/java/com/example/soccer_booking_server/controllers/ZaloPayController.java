package com.example.soccer_booking_server.controllers;

import com.example.soccer_booking_server.services.ZaloPayService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/zalopay")
public class ZaloPayController {

    private final ZaloPayService zaloPayService;

    public ZaloPayController(ZaloPayService zaloPayService) {
        this.zaloPayService = zaloPayService;
    }

    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody Map<String, Object> body) {
        long amount = Long.parseLong(String.valueOf(body.getOrDefault("amount", "50000")));
        String userId = String.valueOf(body.getOrDefault("userId", "user123"));

        // ✅ app_user giống sample: "user123" (bạn có thể truyền userId vào đây)
        return zaloPayService.createOrder(amount, userId);
    }
}
