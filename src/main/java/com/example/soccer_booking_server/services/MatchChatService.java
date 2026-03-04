package com.example.soccer_booking_server.services;

import com.example.soccer_booking_server.dto.chat.ChatMessageDTO;
import com.example.soccer_booking_server.entitis.Users;
import com.example.soccer_booking_server.enums.MatchPostStatus;
import com.example.soccer_booking_server.repository.MatchPostRepository;
import com.example.soccer_booking_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MatchChatService {

    private final UserRepository userRepository;
    private final MatchPostRepository matchPostRepository; // bạn đã có repo này

    @Transactional(readOnly = true)
    public ChatMessageDTO validateAndBuildMessage(Long postId, String senderEmail, String content) {
        var post = matchPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("POST_NOT_FOUND"));

        if (post.getStatus() != MatchPostStatus.MATCHED) {
            throw new RuntimeException("CHAT_NOT_AVAILABLE");
        }

        if (post.getMatchedAt() == null) {
            throw new RuntimeException("MATCHED_AT_MISSING");
        }

        LocalDateTime expireAt = post.getMatchedAt().plusHours(24);
        if (LocalDateTime.now().isAfter(expireAt)) {
            throw new RuntimeException("CHAT_EXPIRED");
        }

        Users sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        Integer senderId = sender.getUserId();
        Integer ownerId = post.getOwner().getUserId();
        Integer opponentId = post.getMatchedUser() == null ? null : post.getMatchedUser().getUserId();

        boolean allowed = senderId.equals(ownerId) || (opponentId != null && senderId.equals(opponentId));
        if (!allowed) {
            throw new RuntimeException("CHAT_FORBIDDEN");
        }

        String trimmed = content == null ? "" : content.trim();
        if (trimmed.isEmpty()) {
            throw new RuntimeException("EMPTY_MESSAGE");
        }
        if (trimmed.length() > 500) {
            throw new RuntimeException("MESSAGE_TOO_LONG");
        }

        return ChatMessageDTO.builder()
                .postId(postId)
                .fromUserId(senderId)
                .fromName(sender.getFullName())
                .content(trimmed)
                .sentAt(LocalDateTime.now())
                .build();
    }
}