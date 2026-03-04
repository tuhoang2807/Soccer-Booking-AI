package com.example.soccer_booking_server.payload.match;

import lombok.Data;

@Data
public class CreateMatchPostRequest {
    private Long bookingId;
    private String title;
    private String note;
}