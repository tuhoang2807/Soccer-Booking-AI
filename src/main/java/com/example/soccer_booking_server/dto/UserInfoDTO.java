package com.example.soccer_booking_server.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    private Integer userId;
    private String fullName;
    private String email;
    private String phone;
    private String teamName;
    private String teamLeaderName;
    private BigDecimal coinBalance;
}
