package com.example.soccer_booking_server.controllers;

import com.example.soccer_booking_server.entitis.Users;
import com.example.soccer_booking_server.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;

@Controller
@RequiredArgsConstructor
public class MatchChatWsController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    /**
     * FE SEND: /app/match/{postId}/send
     * FE SUB : /topic/match/{postId}
     */
    @MessageMapping("/match/{postId}/send")
    public void send(
            @DestinationVariable Long postId,
            @Payload ChatSendRequest req,
            Principal principal
    ) {
        // principal.getName() trong hệ thống bạn đang là EMAIL (sub trong JWT)
        String email = principal != null ? principal.getName() : null;

        Users u = null;
        if (email != null) {
            u = userRepository.findByEmail(email).orElse(null);
        }

        Integer fromUserId = (u != null ? u.getUserId() : null);
        String fromName = (u != null ? u.getFullName() : (email != null ? email : "unknown"));
        String avatarUrl = (u != null ? u.getAvatarUrl() : null);

        ChatMessagePayload payload = new ChatMessagePayload(
                postId,
                fromUserId,
                fromName,
                avatarUrl,
                req.getContent(),
                Instant.now().toString()
        );

        // broadcast cho cả phòng
        messagingTemplate.convertAndSend("/topic/match/" + postId, payload);
    }

    @Data
    public static class ChatSendRequest {
        private String content;
    }

    @Data
    @AllArgsConstructor
    public static class ChatMessagePayload {
        private Long postId;
        private Integer fromUserId;
        private String fromName;
        private String fromAvatarUrl;
        private String content;
        private String sentAt;
    }
}