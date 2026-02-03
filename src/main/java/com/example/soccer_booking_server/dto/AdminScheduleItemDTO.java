package com.example.soccer_booking_server.dto;

import com.example.soccer_booking_server.enums.BookingStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminScheduleItemDTO {
    private Long bookingId;

    private Integer fieldId;
    private String fieldName;

    private Integer slotId;
    private Integer slotNumber;
    private LocalTime slotStart;
    private LocalTime slotEnd;

    private LocalDate bookingDate;
    private BookingStatus status;

    private Integer userId;
    private String userFullName;
    private String userPhone;
}
