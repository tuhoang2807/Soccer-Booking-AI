package com.example.soccer_booking_server.controllers;

import com.example.soccer_booking_server.zalopay.ZaloPayProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/zalopay")
public class ZaloPayCallbackController {

    private final ZaloPayProperties props;
    private final ObjectMapper mapper;

    public ZaloPayCallbackController(ZaloPayProperties props, ObjectMapper mapper) {
        this.props = props;
        this.mapper = mapper;
    }

    @PostMapping("/callback")
    public Map<String, Object> callback(@RequestBody Map<String, Object> body) {
        String data = (String) body.get("data");
        String mac = (String) body.get("mac");

        if (data == null || mac == null) {
            return Map.of("return_code", -1, "return_message", "missing data/mac");
        }

        String expected = com.example.soccer_booking_server.zalopay.HmacUtil.hmacSha256Hex(props.key2(), data);
        if (!expected.equalsIgnoreCase(mac)) {
            return Map.of("return_code", -1, "return_message", "mac not match");
        }

        // Parse data (JSON string)
        try {
            var node = mapper.readTree(data);
            String appTransId = node.path("app_trans_id").asText(null);

            // TODO: update DB: set PAID/FAILED theo node fields
            // TODO: idempotency: nếu đã PAID thì ignore callback lần sau

            return Map.of("return_code", 1, "return_message", "success");
        } catch (Exception e) {
            return Map.of("return_code", 0, "return_message", "parse data error");
        }
    }
}
