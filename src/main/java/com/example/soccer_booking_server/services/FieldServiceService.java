package com.example.soccer_booking_server.services;

import com.example.soccer_booking_server.entitis.FieldServiceEntity;
import com.example.soccer_booking_server.repository.FieldServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FieldServiceService {
    private final FieldServiceRepository fieldServiceRepository;

    public List<FieldServiceEntity> getAll() {
        return fieldServiceRepository.findAll();
    }

    public FieldServiceEntity getById(Integer id) {
        return fieldServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
    }

    public FieldServiceEntity create(FieldServiceEntity service) {
        return fieldServiceRepository.save(service);
    }

    public FieldServiceEntity update(Integer id, FieldServiceEntity data) {
        FieldServiceEntity entity = getById(id);
        entity.setServiceName(data.getServiceName());
        entity.setPrice(data.getPrice());
        return fieldServiceRepository.save(entity);
    }

    public void delete(Integer id) {
        fieldServiceRepository.deleteById(id);
    }
}
