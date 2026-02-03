package com.example.soccer_booking_server.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBookingStatsDTO {
    private long totalBookings;
    private long cancelled;
    private long expired;

    private BigDecimal grossTotal;   // tổng totalPrice
    private BigDecimal paidTotal;    // tổng paidAmount

    private long pendingDeposit;
    private long deposited;
    private long checkedIn;
}
