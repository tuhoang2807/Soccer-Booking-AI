package com.example.soccer_booking_server.services;

import com.example.soccer_booking_server.entitis.Field;
import com.example.soccer_booking_server.exception.NotFoundException;
import com.example.soccer_booking_server.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FieldService {
    private final FieldRepository fieldRepository;

    public List<Field> getAllField() {
        return fieldRepository.findAll();
    }

    public Field getFieldById(Integer id) {
        return fieldRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Field not found"));
    }

    public Field createField(Field field) {
        return fieldRepository.save(field);
    }

    public Field updateField(Integer id, Field updatedField) {
        Field existingField = fieldRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sân!"));

        existingField.setFieldName(updatedField.getFieldName());
        existingField.setType(updatedField.getType());
        existingField.setDescription(updatedField.getDescription());
        existingField.setImageUrl(updatedField.getImageUrl());
        existingField.setPricePerSlot(updatedField.getPricePerSlot());
        existingField.setStatus(updatedField.getStatus());

        return fieldRepository.save(existingField);
    }

    public void deleteField(Integer id) {
        fieldRepository.deleteById(id);
    }
}
