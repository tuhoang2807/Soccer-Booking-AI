package com.example.soccer_booking_server.controllers;
import com.example.soccer_booking_server.dto.*;
import com.example.soccer_booking_server.enums.BookingStatus;
import com.example.soccer_booking_server.services.AdminBookingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final AdminBookingService adminBookingService;

    @Operation(summary = "Admin - Danh sách booking (paging + filter + search)")
    @GetMapping
    public Map<String, Object> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,

            @RequestParam(required = false) Integer fieldId,
            @RequestParam(required = false) Integer slotId,
            @RequestParam(required = false) Integer userId,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @RequestParam(required = false) List<BookingStatus> statuses,
            @RequestParam(required = false) String q
    ) {
        Page<AdminBookingListItemDTO> data = adminBookingService.list(
                page, size, fieldId, slotId, userId, from, to, statuses, q
        );
        return Map.of("data", data);
    }

    @Operation(summary = "Admin - Chi tiết booking")
    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        return Map.of("data", adminBookingService.detail(id));
    }

    @Operation(summary = "Admin - Thanh toán nốt (trừ coin user, tăng paidAmount)")
    @PostMapping("/{id}/mark-paid")
    public Map<String, Object> markPaid(@PathVariable Long id, @RequestBody(required = false) AdminMarkPaidRequest req) {
        adminBookingService.markPaid(id, req);
        return Map.of("message", "Mark paid success");
    }

    @Operation(summary = "Admin - Hoàn tiền (cộng coin user, giảm paidAmount)")
    @PostMapping("/{id}/refund")
    public Map<String, Object> refund(@PathVariable Long id, @RequestBody(required = false) AdminRefundRequest req) {
        adminBookingService.refund(id, req);
        return Map.of("message", "Refund success");
    }

    @Operation(summary = "Admin - Force expire booking")
    @PostMapping("/{id}/expire")
    public Map<String, Object> expire(@PathVariable Long id, @RequestParam(required = false) String reason) {
        adminBookingService.forceExpire(id, reason);
        return Map.of("message", "Expire success");
    }

    @Operation(summary = "Admin - Cập nhật ghi chú")
    @PatchMapping("/{id}/note")
    public Map<String, Object> note(@PathVariable Long id, @RequestBody AdminNoteRequest req) {
        adminBookingService.updateNote(id, req);
        return Map.of("message", "Update note success");
    }

    @Operation(summary = "Admin - Lịch booking theo ngày (cho calendar view)")
    @GetMapping("/schedule")
    public Map<String, Object> schedule(
            @RequestParam(required = false) Integer fieldId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return Map.of("data", adminBookingService.schedule(fieldId, date));
    }

    @Operation(summary = "Admin - Thống kê booking (from/to)")
    @GetMapping("/stats")
    public Map<String, Object> stats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return Map.of("data", adminBookingService.stats(from, to));
    }
}

