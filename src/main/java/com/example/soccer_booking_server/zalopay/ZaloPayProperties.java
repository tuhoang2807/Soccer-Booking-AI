package com.example.soccer_booking_server.zalopay;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zalopay")
public record ZaloPayProperties(
        int appId,
        String key1,
        String key2,
        String createEndpoint,
        String callbackUrl
) {}
