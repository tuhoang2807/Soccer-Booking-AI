package com.example.soccer_booking_server.repository;

import com.example.soccer_booking_server.entitis.FieldSlot;
import com.example.soccer_booking_server.projection.FieldSlotProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FieldSlotRepository extends JpaRepository<FieldSlot,Integer> {
    @Query("""
        SELECT 
            fs.slotId AS slotId,
            fs.slotNumber AS slotNumber,
            fs.slotStart AS slotStart,
            fs.slotEnd AS slotEnd,
            fs.price AS price,
            fs.isPeak AS isPeak
        FROM FieldSlot fs
    """)
    List<FieldSlotProjection> findAllSlotsProjection();
}
