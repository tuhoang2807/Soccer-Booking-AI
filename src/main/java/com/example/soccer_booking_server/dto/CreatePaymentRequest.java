package com.example.soccer_booking_server.dto;

public record CreatePaymentRequest(
        long amount,
        String userId,
        String description
) {}
