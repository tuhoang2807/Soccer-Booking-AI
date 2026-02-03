package com.example.soccer_booking_server.controllers;

import com.example.soccer_booking_server.dto.CreateBookingRequest;
import com.example.soccer_booking_server.enums.BookingStatus;
import com.example.soccer_booking_server.services.BookingAppService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingAppService bookingService;

    @Operation(
            summary = "Lấy danh sách slot đã bị đặt theo sân và ngày (dùng để bôi đen slot trên UI)"
    )
    @GetMapping("/occupied")
    public Map<String, Object> occupied(
            @RequestParam Integer fieldId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return Map.of(
                "data", bookingService.getOccupiedSlots(fieldId, date)
        );
    }

    @Operation(
            summary = "Tạo đơn đặt sân (trạng thái PENDING_DEPOSIT – giữ chỗ 20 phút chờ cọc)"
    )
    @PostMapping
    public Map<String, Object> create(
            @RequestBody CreateBookingRequest req
    ) {
        return Map.of(
                "data", bookingService.createBooking(req)
        );
    }

    @Operation(
            summary = "Thực hiện cọc sân (trừ coin 30% tiền sân, chuyển trạng thái sang DEPOSITED)"
    )
    @PostMapping("/{id}/deposit")
    public Map<String, Object> deposit(
            @PathVariable("id") Long bookingId
    ) {
        bookingService.deposit(bookingId);
        return Map.of(
                "message", "Deposit success"
        );
    }

    @Operation(
            summary = "Check-in khi khách đến sân (chỉ áp dụng cho booking đã cọc)"
    )
    @PostMapping("/{id}/checkin")
    public Map<String, Object> checkin(
            @PathVariable("id") Long bookingId
    ) {
        bookingService.checkIn(bookingId);
        return Map.of(
                "message", "Check-in success"
        );
    }

    @Operation(
            summary = "Huỷ đơn đặt sân (do người dùng hoặc admin)"
    )
    @PostMapping("/{id}/cancel")
    public Map<String, Object> cancel(
            @PathVariable("id") Long bookingId
    ) {
        bookingService.cancel(bookingId);
        return Map.of(
                "message", "Cancel success"
        );
    }


    @Operation(summary = "Lấy lịch sử đặt sân theo user (tạm thời truyền userId)")
    @GetMapping("/history")
    public Map<String, Object> history(
            @RequestParam Integer userId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) List<BookingStatus> statuses
    ) {
        Page<?> data = bookingService.getUserHistory(userId, page, size, statuses);
        return Map.of("data", data);
    }
}
