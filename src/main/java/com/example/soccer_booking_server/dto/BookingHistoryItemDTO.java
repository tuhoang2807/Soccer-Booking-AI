package com.example.soccer_booking_server.dto;

import com.example.soccer_booking_server.enums.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingHistoryItemDTO {
    private Long bookingId;

    private Integer fieldId;
    private String fieldName;

    private Integer slotId;
    private Integer slotNumber;
    private LocalTime slotStart;
    private LocalTime slotEnd;

    private LocalDate bookingDate;
    private BookingStatus status;

    private BigDecimal fieldPrice;
    private BigDecimal serviceTotal;
    private BigDecimal depositAmount;
    private BigDecimal paidAmount;
    private BigDecimal totalPrice;

    private LocalDateTime createdAt;
}
