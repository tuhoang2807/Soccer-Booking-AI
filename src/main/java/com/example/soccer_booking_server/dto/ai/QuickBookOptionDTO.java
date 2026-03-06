package com.example.soccer_booking_server.dto.ai;

import com.example.soccer_booking_server.enums.FieldType;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuickBookOptionDTO {
    private Integer fieldId;
    private String fieldName;
    private FieldType fieldType;

    private Integer slotId;
    private Integer slotNumber;
    private LocalTime slotStart;
    private LocalTime slotEnd;

    private LocalDate bookingDate;
    private Double score;
}