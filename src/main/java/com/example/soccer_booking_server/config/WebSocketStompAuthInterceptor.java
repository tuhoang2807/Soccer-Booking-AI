package com.example.soccer_booking_server.config;

import com.example.soccer_booking_server.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class WebSocketStompAuthInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(acc.getCommand())) {

            String auth = acc.getFirstNativeHeader("Authorization");
            if (!StringUtils.hasText(auth)) auth = acc.getFirstNativeHeader("authorization");
            if (!StringUtils.hasText(auth)) auth = acc.getFirstNativeHeader("token");

            String token = null;
            if (StringUtils.hasText(auth)) {
                token = auth.startsWith("Bearer ") ? auth.substring(7) : auth;
            }

            if (!StringUtils.hasText(token) || !jwtUtils.validateJwtToken(token)) {
                throw new IllegalArgumentException("WS_UNAUTHORIZED");
            }

            String email = jwtUtils.getEmailFromToken(token);

            // ✅ set Principal chuẩn
            UsernamePasswordAuthenticationToken user =
                    new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());

            acc.setUser(user);
            SecurityContextHolder.getContext().setAuthentication(user);
        }

        return message;
    }
}