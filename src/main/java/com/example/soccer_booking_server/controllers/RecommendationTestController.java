package com.example.soccer_booking_server.controllers;

import com.example.soccer_booking_server.dto.ai.AiPreferencesResponse;
import com.example.soccer_booking_server.services.ai.AiRecommenderClient;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationTestController {

    private final AiRecommenderClient aiClient;

    @GetMapping("/test-ai")
    public AiPreferencesResponse testAi(@RequestParam Integer userId) {
        // TẠM THỜI: hardcode giống bạn test trên Swagger
        List<Map<String, Object>> history = List.of(
                Map.of("user_id", userId, "booking_date", "2026-03-01", "slot_id", 5, "field_type", "SEVEN", "field_id", 3),
                Map.of("user_id", userId, "booking_date", "2026-03-08", "slot_id", 5, "field_type", "SEVEN", "field_id", 3),
                Map.of("user_id", userId, "booking_date", "2026-03-10", "slot_id", 6, "field_type", "SEVEN", "field_id", 5)
        );

        return aiClient.getPreferences(userId, history);
    }
}