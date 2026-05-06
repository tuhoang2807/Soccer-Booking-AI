package com.example.soccer_booking_server.controllers;

import com.example.soccer_booking_server.dto.BookingHistoryItemDTO;
import com.example.soccer_booking_server.dto.ai.AiPreferencesResponse;
import com.example.soccer_booking_server.enums.BookingStatus;
import com.example.soccer_booking_server.services.BookingAppService;
import com.example.soccer_booking_server.services.ai.AiRecommenderClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationTestController {

    private final AiRecommenderClient aiClient;
    private final BookingAppService bookingAppService;

    @GetMapping("/test-ai")
    public AiPreferencesResponse testAi(@RequestParam Integer userId) {

        List<BookingStatus> goodStatuses = List.of(
                BookingStatus.DEPOSITED,
                BookingStatus.CHECKED_IN,
                BookingStatus.COMPLETED
        );

        var page = bookingAppService.getUserHistory(userId, 0, 200, goodStatuses);
        List<BookingHistoryItemDTO> history = page.getContent();

        List<Map<String, Object>> userBookings = history.stream().map(h -> {
            Map<String, Object> m = new HashMap<>();
            m.put("user_id", userId);
            m.put("booking_date", h.getBookingDate().toString());
            m.put("slot_id", h.getSlotId());
            m.put("field_id", h.getFieldId());
            return m;
        }).collect(Collectors.toList());

        return aiClient.getPreferences(userId, userBookings);
    }
}