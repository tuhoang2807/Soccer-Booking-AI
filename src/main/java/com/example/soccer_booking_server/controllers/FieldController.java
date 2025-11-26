package com.example.soccer_booking_server.controllers;


import com.example.soccer_booking_server.dto.ResponseFormat;
import com.example.soccer_booking_server.entitis.Field;
import com.example.soccer_booking_server.services.FieldService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/v1/field")
@RequiredArgsConstructor
public class FieldController {
    private final FieldService fieldService;

    @Operation(summary = "Get all field")
    @PermitAll
    @GetMapping
    public ResponseEntity<ResponseFormat<List<Field>>> getAllFields() {
        return ResponseEntity.ok(new ResponseFormat<>(200, "Thành công", fieldService.getAllField()));
    }

    @Operation(summary = "Get field by id")
    @PermitAll
    @GetMapping("/{id}")
    public ResponseEntity<ResponseFormat<Field>> getFieldById(@PathVariable int id) {
        return ResponseEntity.ok(new  ResponseFormat<>(200, "Thành công", fieldService.getFieldById(id)));
    }

    @Operation(summary = "Delete field by id")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseFormat<?>> deleteField(@PathVariable Integer id) {
        fieldService.deleteField(id);
        return ResponseEntity.ok(new ResponseFormat<Void>(200, "Xóa sân thành công!", null));
    }

    @Operation(summary = "Create field")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PostMapping
    public ResponseEntity<ResponseFormat<Field>> createField(@RequestBody Field newField) {
        return ResponseEntity.ok(new ResponseFormat<>(201, "Tạo sân bóng thành công!", fieldService.createField(newField)));
    }

    @Operation(summary = "Update field")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseFormat<Field>> updateField(@PathVariable Integer id,@RequestBody Field updatedField) {
        return ResponseEntity.ok(new ResponseFormat<>(201, "Cập nhật sân bóng thành công!",fieldService.updateField(id,updatedField)));
    }
}
