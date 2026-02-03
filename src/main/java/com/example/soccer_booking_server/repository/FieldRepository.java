package com.example.soccer_booking_server.repository;

import com.example.soccer_booking_server.entitis.Field;
import com.example.soccer_booking_server.enums.FieldType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldRepository extends JpaRepository<Field, Integer> {
    List<Field> findByType(FieldType type);
}
