package com.example.soccer_booking_server.entitis;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "field_service")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private Integer serviceId;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(nullable = false)
    private BigDecimal price;
}
