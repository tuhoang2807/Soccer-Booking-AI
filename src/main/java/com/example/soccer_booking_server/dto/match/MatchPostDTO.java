package com.example.soccer_booking_server.dto.match;

import com.example.soccer_booking_server.enums.MatchPostStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MatchPostDTO {
    private Long id;
    private Long bookingId;
    private Integer ownerId;
    private MatchPostStatus status;
    private Integer matchedUserId;
    private String title;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime matchedAt;
}