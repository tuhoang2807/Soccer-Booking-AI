package com.example.soccer_booking_server.dto;

import com.example.soccer_booking_server.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CreateBookingResponse {
    private Long bookingId;
    private BookingStatus status;
    private BigDecimal fieldPrice;
    private BigDecimal serviceTotal;
    private BigDecimal depositAmount;
    private BigDecimal totalPrice;
    private LocalDateTime depositDueAt;
    private LocalDateTime checkinDueAt;
}
