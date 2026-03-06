package com.example.soccer_booking_server.services.ai;

import com.example.soccer_booking_server.dto.ai.AiPreferencesResponse;
import java.util.List;
import java.util.Map;

import com.example.soccer_booking_server.dto.ai.AiRecommendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AiRecommenderClient {

    private final RestTemplate restTemplate;

    @Value("${ai.recommender.base-url}")
    private String baseUrl;

    public AiPreferencesResponse getPreferences(Integer userId, List<Map<String, Object>> userBookings) {
        String url = baseUrl + "/preferences";

        Map<String, Object> payload = Map.of(
                "user_id", userId,
                "user_bookings", userBookings
        );

        return restTemplate.postForObject(url, payload, AiPreferencesResponse.class);
    }

    public AiRecommendResponse recommend(
            Integer userId,
            List<Map<String, Object>> userBookings,
            List<Map<String, Object>> candidates,
            Integer topK
    ) {
        String url = baseUrl + "/recommend";
        Map<String, Object> payload = Map.of(
                "user_id", userId,
                "user_bookings", userBookings,
                "candidates", candidates,
                "top_k", topK
        );
        return restTemplate.postForObject(url, payload, AiRecommendResponse.class);
    }
}