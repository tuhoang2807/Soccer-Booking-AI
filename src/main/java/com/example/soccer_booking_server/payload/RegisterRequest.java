package com.example.soccer_booking_server.payload;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RegisterRequest {
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String teamName;
    private String teamLeadName;
}
