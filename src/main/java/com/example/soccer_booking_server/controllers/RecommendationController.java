package com.example.soccer_booking_server.controllers;

import com.example.soccer_booking_server.dto.ai.QuickBookOptionDTO;
import com.example.soccer_booking_server.services.ai.QuickBookService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final QuickBookService quickBookService;

    @GetMapping("/quick-book")
    public Map<String, Object> quickBook(
            @RequestParam Integer userId,
            @RequestParam(defaultValue = "7") Integer days,
            @RequestParam(defaultValue = "10") Integer topK
    ) {
        List<QuickBookOptionDTO> data = quickBookService.quickBook(userId, days, topK);
        return Map.of("data", data);
    }
}