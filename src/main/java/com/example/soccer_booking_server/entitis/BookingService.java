package com.example.soccer_booking_server.entitis;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "booking_service",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"booking_id", "service_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_service_id")
    private Long bookingServiceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private FieldServiceEntity service;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    // snapshot giá tại thời điểm đặt
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false)
    private BigDecimal lineTotal;
}
