package com.example.soccer_booking_server.dto.chat;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private Long postId;
    private Integer fromUserId;
    private String fromName;
    private String content;
    private LocalDateTime sentAt;
}