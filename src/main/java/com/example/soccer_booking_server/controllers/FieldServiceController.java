package com.example.soccer_booking_server.controllers;

import com.example.soccer_booking_server.entitis.FieldServiceEntity;
import com.example.soccer_booking_server.services.FieldServiceService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/field-services")
@RequiredArgsConstructor
public class FieldServiceController {

    private final FieldServiceService fieldServiceService;

    @Operation(summary = "Get all service")
    @GetMapping
    public List<FieldServiceEntity> getAll() {
        return fieldServiceService.getAll();
    }

    @Operation(summary = "Get service by id")
    @GetMapping("/{id}")
    public FieldServiceEntity getById(@PathVariable Integer id) {
        return fieldServiceService.getById(id);
    }

    @Operation(summary = "Create new service")
    @PostMapping
    public FieldServiceEntity create(@RequestBody FieldServiceEntity service) {
        return fieldServiceService.create(service);
    }

    @Operation(summary = "update service")
    @PutMapping("/{id}")
    public FieldServiceEntity update(
            @PathVariable Integer id,
            @RequestBody FieldServiceEntity service) {
        return fieldServiceService.update(id, service);
    }

    @Operation(summary = "Delete service")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        fieldServiceService.delete(id);
    }
}
