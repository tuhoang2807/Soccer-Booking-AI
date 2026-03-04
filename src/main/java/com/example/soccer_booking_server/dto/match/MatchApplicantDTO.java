package com.example.soccer_booking_server.dto.match;

import com.example.soccer_booking_server.enums.MatchApplicationStatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchApplicantDTO {
    private Integer id;
    private String name;
    private String avatarUrl;
    private String message;
    private MatchApplicationStatus status;
}