package com.example.soccer_booking_server.repository;

import com.example.soccer_booking_server.entitis.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldRepository extends JpaRepository<Field, Integer> {
}
