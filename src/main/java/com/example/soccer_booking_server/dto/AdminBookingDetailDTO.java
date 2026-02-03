package com.example.soccer_booking_server.dto;


import com.example.soccer_booking_server.enums.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBookingDetailDTO {
    private Long bookingId;

    private Integer userId;
    private String userFullName;
    private String userEmail;
    private String userPhone;

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

    private LocalDateTime depositDueAt;
    private LocalDateTime checkinDueAt;
    private LocalDateTime checkedInAt;

    private String expireReason;

    private String adminNote;

    private List<ServiceLineDTO> services;

    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceLineDTO {
        private Integer serviceId;
        private String serviceName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
    }
}
