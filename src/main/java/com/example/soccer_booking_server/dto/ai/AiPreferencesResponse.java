package com.example.soccer_booking_server.dto.ai;

import java.util.List;
import lombok.Data;

@Data
public class AiPreferencesResponse {
    private Integer user_id;
    private List<Integer> weekdays;
    private List<Integer> slots;
    private List<String> field_types;
    private List<Integer> fields;
}