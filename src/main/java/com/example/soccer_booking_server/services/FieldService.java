package com.example.soccer_booking_server.services;

import com.example.soccer_booking_server.entitis.Field;
import com.example.soccer_booking_server.enums.FieldType;
import com.example.soccer_booking_server.exception.NotFoundException;
import com.example.soccer_booking_server.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
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
                .orElseThrow(() -> new NotFoundException("Không có sân này!"));
    }

    public List<Field> getFieldByType(String type) {
        FieldType fieldType;
        try {
            fieldType = FieldType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "type không hợp lệ. Ví dụ: SEVEN, FIVE, TEN"
            );
        }
        return fieldRepository.findByType(fieldType);
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
        existingField.setStatus(updatedField.getStatus());

        return fieldRepository.save(existingField);
    }

    public void deleteField(Integer id) {
        fieldRepository.deleteById(id);
    }
}
