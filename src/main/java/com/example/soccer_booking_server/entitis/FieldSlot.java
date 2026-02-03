package com.example.soccer_booking_server.entitis;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "field_slot",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"field_id", "slot_number"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slot_id")
    private Integer slotId;

    @Column(name = "slot_number", nullable = false)
    private Integer slotNumber;

    @Column(name = "slot_start", nullable = false)
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime slotStart;

    @Column(name = "slot_end", nullable = false)
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime slotEnd;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "is_peak", nullable = false)
    private Boolean isPeak = false;
}
