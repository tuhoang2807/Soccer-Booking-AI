package com.example.soccer_booking_server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FieldSlotDTO {
    private Integer slotId;
    private Integer slotNumber;
    private LocalTime slotStart;
    private LocalTime slotEnd;
    private BigDecimal price;
    private Boolean isPeak;
}
