package com.example.soccer_booking_server.repository;


import com.example.soccer_booking_server.entitis.FieldServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FieldServiceRepository
        extends JpaRepository<FieldServiceEntity, Integer> {
}
