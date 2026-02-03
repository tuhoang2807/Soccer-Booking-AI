package com.example.soccer_booking_server.dto;

public record CreatePaymentResponse(
        String appTransId,
        String orderUrl,
        String zpTransToken,
        int returnCode,
        String returnMessage,
        Integer subReturnCode,
        String subReturnMessage
) {}
