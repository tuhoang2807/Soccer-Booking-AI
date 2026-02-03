package com.example.soccer_booking_server.controllers;
import com.example.soccer_booking_server.dto.ResponseFormat;
import com.example.soccer_booking_server.entitis.Users;
import com.example.soccer_booking_server.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping
    public ResponseEntity<ResponseFormat<List<Users>>> getAllUsers() {
        List<Users> users = userService.getAllUsers();
        return ResponseEntity.ok(new ResponseFormat<>(200, "Thành công", users));
    }

    @Operation(summary = "Get user coin")
    @PermitAll
    @GetMapping("/coin/{id}")
    public ResponseEntity<ResponseFormat<BigDecimal>> getUserCoin(@PathVariable Integer id) {

        BigDecimal coin = userService.getUserCoin(id);
        return ResponseEntity.ok(new ResponseFormat<>(200, "Thành công", coin));
    }


    @Operation(summary = "Get user by id")
    @PermitAll
    @GetMapping("/{id}")
    public ResponseEntity<ResponseFormat<Users>> getUserById(@PathVariable Integer id) {
        Users user = userService.getUserById(id);
        return ResponseEntity.ok(new ResponseFormat<>(200, "Thành công", user));
    }

    @Operation(summary = "Create new user")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PostMapping
    public ResponseEntity<ResponseFormat<Users>> createUser(@RequestBody Users user) {
        Users createdUser = userService.createUser(user);
        return ResponseEntity.ok(new  ResponseFormat<>(201, "Tạo người dùng mới thành công!", createdUser));
    }

    @Operation(summary = "Update user by id")
    @PermitAll
    @PutMapping("/{id}")
    public ResponseEntity<ResponseFormat<Users>> updateUser(@PathVariable Integer id, @RequestBody Users user) {
        Users updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(new ResponseFormat<>(201, "Cập nhật người dùng thành công!", updatedUser));
    }

    @Operation(summary = "Delete user by id")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseFormat<?>> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new ResponseFormat<Void>(200, "Xóa người dùng thành công!", null));
    }
}

