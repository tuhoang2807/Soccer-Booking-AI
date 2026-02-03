package com.example.soccer_booking_server.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

// CreateBookingRequest.java
@Data
public class CreateBookingRequest {
    private Integer userId;
    private Integer fieldId;
    private Integer slotId;
    private LocalDate bookingDate;
    private List<Integer> serviceIds; // optional
    private String couponCode;        // optional (demo)
}

