package com.example.soccer_booking_server.payload;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String email;
}