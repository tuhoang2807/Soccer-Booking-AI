package com.example.soccer_booking_server.controllers;

import com.example.soccer_booking_server.dto.ResponseFormat;
import com.example.soccer_booking_server.entitis.FieldSlot;
import com.example.soccer_booking_server.projection.FieldSlotProjection;
import com.example.soccer_booking_server.services.FieldSlotService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/v1/fieldSlot")
@RequiredArgsConstructor
public class FieldSlotController {
    private final FieldSlotService fieldSlotService;

    @Operation(summary = "Get all slot")
    @PermitAll
    @GetMapping
    public ResponseEntity<ResponseFormat<List<FieldSlotProjection>>> getAllSlot() {
        return ResponseEntity.ok(new ResponseFormat<>(200,"Thành công", fieldSlotService.getAllSlot()));
    }

    @Operation(summary = "Get slot by id")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseFormat<FieldSlot>> getSlotById(@PathVariable Integer id) {
        return ResponseEntity.ok(new ResponseFormat<>(200,"Thành công",fieldSlotService.getSlotById(id)));
    }

    @Operation(summary = "create slot")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PostMapping
    public ResponseEntity<ResponseFormat<FieldSlot>> createSlot(@RequestBody FieldSlot fieldSlot) {
        return ResponseEntity.ok(new ResponseFormat<>(201, "Tạo thành công khung giờ!", fieldSlotService.saveSlot(fieldSlot)));
    }

    @Operation(summary = "delete slot")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseFormat<?>> deleteSlot(@PathVariable Integer id) {
        fieldSlotService.deleteSlot(id);
        return ResponseEntity.ok(new ResponseFormat<Void>(200, "Xóa khung giờ thành công!", null));
    }
}
