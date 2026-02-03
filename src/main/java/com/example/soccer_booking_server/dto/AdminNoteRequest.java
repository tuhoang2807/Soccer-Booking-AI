package com.example.soccer_booking_server.dto;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminNoteRequest {
    private String note;
}
