package com.example.soccer_booking_server.services;

import com.example.soccer_booking_server.entitis.FieldSlot;
import com.example.soccer_booking_server.exception.NotFoundException;
import com.example.soccer_booking_server.projection.FieldSlotProjection;
import com.example.soccer_booking_server.repository.FieldSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FieldSlotService {
    private final FieldSlotRepository fieldSlotRepository;

    public List<FieldSlotProjection> getAllSlot() {
        return fieldSlotRepository.findAllSlotsProjection();
    }

    public FieldSlot getSlotById(Integer id ) {
        return fieldSlotRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không có khung giờ này!"));
    }

    public FieldSlot saveSlot(FieldSlot fieldSlot) {
        return fieldSlotRepository.save(fieldSlot);
    }

    public FieldSlot update(FieldSlot fieldSlot) {
        return fieldSlotRepository.save(fieldSlot);
    }

    public void deleteSlot(Integer id) {
         fieldSlotRepository.deleteById(id);
    }
}
