package com.example.soccer_booking_server.dto.ai;

import java.util.List;
import lombok.Data;

@Data
public class AiRecommendResponse {
    private Integer user_id;
    private List<Recommendation> recommendations;

    @Data
    public static class Recommendation {
        private String booking_date;
        private Integer slot_id;
        private Integer field_id;
        private Double score;
    }
}