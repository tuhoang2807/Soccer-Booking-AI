package com.example.soccer_booking_server.payload.match;

import lombok.Data;

@Data
public class ChooseOpponentRequest {
    private Integer applicantUserId; // vì Users.userId của bạn là Integer
}